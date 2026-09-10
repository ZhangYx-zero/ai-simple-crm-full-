package com.aicrm.init;

import com.aicrm.entity.CrmContact;
import com.aicrm.entity.CrmCustomer;
import com.aicrm.entity.CrmFollowUp;
import com.aicrm.entity.CrmLead;
import com.aicrm.entity.CrmOpportunity;
import com.aicrm.entity.SysUser;
import com.aicrm.mapper.CrmContactMapper;
import com.aicrm.mapper.CrmCustomerMapper;
import com.aicrm.mapper.CrmFollowUpMapper;
import com.aicrm.mapper.CrmLeadMapper;
import com.aicrm.mapper.CrmOpportunityMapper;
import com.aicrm.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 启动时初始化演示数据（幂等：已存在则跳过，重启不会重复造）。
 *
 * 为什么用户用 Java 插入、不用 schema.sql？
 * 因为密码要存 BCrypt 密文，而 BCrypt 每次盐值随机，不能在 SQL 里写死——
 * 用 Java 在运行时 passwordEncoder.encode("123456") 生成最省事。
 *
 * 造的数据尽量覆盖"三种角色 + 各阶段商机"，让漏斗/AI/数据权限开箱即有内容可看：
 *   用户：admin(ADMIN)/manager(MANAGER)/seller(SALES)，密码都是 123456
 *   业务：客户分属 seller 与 admin；商机覆盖阶段 1~6（含 1 赢 1 输）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String PASSWORD = "123456";

    private final SysUserMapper sysUserMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CrmCustomerMapper customerMapper;
    private final CrmContactMapper contactMapper;
    private final CrmLeadMapper leadMapper;
    private final CrmOpportunityMapper opportunityMapper;
    private final CrmFollowUpMapper followUpMapper;

    @Override
    public void run(String... args) {
        initUsers();

        // 业务演示数据：只要已存在任意客户就整体跳过（幂等）
        Long customerCount = customerMapper.selectCount(Wrappers.<CrmCustomer>lambdaQuery());
        if (customerCount != null && customerCount > 0) {
            log.info("已存在客户数据，跳过演示业务数据初始化");
            return;
        }
        initBusinessData();
    }

    // ==================== 用户 ====================

    private void initUsers() {
        insertUserIfAbsent("admin", "系统管理员", "ADMIN");
        insertUserIfAbsent("manager", "销售主管老李", "MANAGER");
        insertUserIfAbsent("seller", "销售小王", "SALES");
    }

    private void insertUserIfAbsent(String username, String nickname, String role) {
        Long count = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (count != null && count > 0) {
            return;
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(1);
        sysUserMapper.insert(user);
        log.info("初始化演示用户：{} / {} / 角色 {} / 密码 {}", username, nickname, role, PASSWORD);
    }

    // ==================== 业务演示数据 ====================

    private void initBusinessData() {
        Long seller = userId("seller");
        Long admin = userId("admin");
        Long manager = userId("manager");

        // ---------- 客户（seller 名下 3 个，admin 名下 2 个，状态/等级混合）----------
        CrmCustomer c1 = insertCustomer("杭州云启科技有限公司", "企业服务", "A", 1, seller, "0571-88886666", "杭州市余杭区");
        CrmCustomer c2 = insertCustomer("上海悦动体育用品", "零售", "B", 1, seller, "021-66668888", "上海市浦东新区");
        CrmCustomer c3 = insertCustomer("苏州精工制造集团", "智能制造", "A", 0, seller, "0512-66660000", "苏州市工业园区");
        CrmCustomer c4 = insertCustomer("北京中科数联", "软件与信息服务", "A", 1, admin, "010-88889999", "北京市海淀区");
        CrmCustomer c5 = insertCustomer("成都天府文创", "文化传媒", "C", 2, admin, "028-66663333", "成都市高新区");

        // ---------- 联系人（每客户 1~2 个）----------
        insertContact(c1.getId(), "赵敏", "采购总监", "13800000001", 1);
        insertContact(c1.getId(), "钱进", "IT经理", "13800000002", 0);
        insertContact(c2.getId(), "孙悦", "运营负责人", "13800000003", 1);
        insertContact(c3.getId(), "李建国", "设备科长", "13800000004", 1);
        insertContact(c4.getId(), "周正", "副总裁", "13800000005", 1);
        insertContact(c5.getId(), "吴芳", "总助", "13800000006", 0);

        // ---------- 线索（seller 名下，状态 0/1）----------
        insertLead("王大力", "深圳新智造电子", "13811110001", "展会", 0, seller);
        insertLead("郑爽", "广州云图软件", "13811110002", "网络", 1, seller);

        // ---------- 商机（覆盖阶段 1~6，金额各不相同；归属跟随客户）----------
        insertOpportunity("2026 云ERP升级", c1.getId(), "120000.00", 1, 20, seller);
        insertOpportunity("余杭新园区弱电项目", c1.getId(), "80000.00", 2, 35, seller);
        insertOpportunity("门店会员系统", c2.getId(), "60000.00", 3, 50, seller);
        insertOpportunity("全国连锁收银改造", c2.getId(), "200000.00", 4, 70, seller);
        insertOpportunity("数字化工厂MES试点", c3.getId(), "150000.00", 1, 15, seller);
        insertOpportunity("中科数联CRM私有化", c4.getId(), "260000.00", 5, 100, admin);   // WIN
        insertOpportunity("中科数联报表BI二期", c4.getId(), "90000.00", 6, 0, admin);       // LOSE
        insertOpportunity("天府文创活动平台", c5.getId(), "50000.00", 2, 30, admin);

        // ---------- 跟进记录 ----------
        insertFollowUp(c1.getId(), null, "电话", "介绍云ERP方案，客户约下周演示", seller);
        insertFollowUp(c2.getId(), null, "见面", "门店会员系统需求确认，客户认可报价方向", seller);
        insertFollowUp(c4.getId(), null, "微信", "确认合同细节，已发正式报价单", admin);

        log.info("演示业务数据初始化完成：5 客户 / 2 线索 / 8 商机 / 3 跟进");
    }

    // ==================== 小工具 ====================

    private Long userId(String username) {
        SysUser u = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        return u == null ? null : u.getId();
    }

    private CrmCustomer insertCustomer(String name, String industry, String level,
                                       Integer status, Long ownerId, String phone, String address) {
        CrmCustomer c = new CrmCustomer();
        c.setName(name);
        c.setIndustry(industry);
        c.setLevel(level);
        c.setStatus(status);
        c.setOwnerId(ownerId);
        c.setPhone(phone);
        c.setAddress(address);
        customerMapper.insert(c);
        return c;
    }

    private void insertContact(Long customerId, String name, String position,
                               String phone, Integer isDecision) {
        CrmContact cc = new CrmContact();
        cc.setCustomerId(customerId);
        cc.setName(name);
        cc.setPosition(position);
        cc.setPhone(phone);
        cc.setIsDecision(isDecision);
        contactMapper.insert(cc);
    }

    private void insertLead(String name, String company, String phone,
                            String source, Integer status, Long ownerId) {
        CrmLead lead = new CrmLead();
        lead.setName(name);
        lead.setCompany(company);
        lead.setPhone(phone);
        lead.setSource(source);
        lead.setStatus(status);
        lead.setOwnerId(ownerId);
        leadMapper.insert(lead);
    }

    private void insertOpportunity(String name, Long customerId, String amount,
                                   Integer stage, Integer winRate, Long ownerId) {
        CrmOpportunity opp = new CrmOpportunity();
        opp.setName(name);
        opp.setCustomerId(customerId);
        opp.setOwnerId(ownerId);
        opp.setAmount(new BigDecimal(amount));
        opp.setStage(stage);
        opp.setWinRate(winRate);
        if (stage == 6) {
            opp.setFailReason("客户预算不足，暂缓采购");
        }
        opportunityMapper.insert(opp);
    }

    private void insertFollowUp(Long customerId, Long opportunityId, String type,
                                String content, Long createBy) {
        CrmFollowUp fu = new CrmFollowUp();
        fu.setCustomerId(customerId);
        fu.setOpportunityId(opportunityId);
        fu.setType(type);
        fu.setContent(content);
        fu.setCreateBy(createBy);
        followUpMapper.insert(fu);
    }
}
