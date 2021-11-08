package com.example.marketplace.controller;

import com.example.marketplace.model.Activity;
import com.example.marketplace.model.LoyaltyProgram;
import com.example.marketplace.model.Reward;
import com.example.marketplace.model.Wallet;
import com.example.marketplace.model.WalletCategory;
import com.example.marketplace.repository.ApplicationDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author ankurmundra
 * November 06, 2021
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {
    private final JdbcTemplate jdbcTemplate;

    private String rewardActivityLpCode;

    public CustomerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String customer() {
        return "customer_landing";
    }

    @GetMapping("/enroll_loyalty_program")
    public String enrollInLoyaltyProgram(Model model) {
        String cid = ApplicationDao.getLoggedInCustomerOrBrand(jdbcTemplate);
        List<LoyaltyProgram> loyaltyPrograms = ApplicationDao.getLoyaltyProgramsNotEnrolled(jdbcTemplate, cid);
        model.addAttribute("loyaltyProgram", new LoyaltyProgram());
        model.addAttribute("programs", loyaltyPrograms);
        return "enroll_loyalty_program";
    }

    @PostMapping("/enroll_loyalty_program")
    public String enrollCustomerInLoyaltyProgram(@ModelAttribute("loyaltyProgram") LoyaltyProgram program) {
        String loggedInCustomer = ApplicationDao.getLoggedInCustomerOrBrand(jdbcTemplate);
        ApplicationDao.enrollCustomer(jdbcTemplate, loggedInCustomer, program.getCode());
        return "redirect:/customer/enroll_loyalty_program?success";
    }

    @GetMapping("/reward_activities")
    public String rewardActivities(Model model) {
        String customer = ApplicationDao.getLoggedInCustomerOrBrand(jdbcTemplate);
        List<LoyaltyProgram> programsList = ApplicationDao.getCustomerEnrolledPrograms(jdbcTemplate, customer);
        model.addAttribute("programs", programsList);
        model.addAttribute("loyaltyProgram", new LoyaltyProgram());
        return "customer_reward_activities";
    }

    @PostMapping("/reward_activities")
    public String displayActivityMenu(@ModelAttribute("loyaltyProgram") LoyaltyProgram program,
                                      @ModelAttribute("rewardActivity") Activity activity, Model model) {
        if (activity == null || activity.getActivityName() == null) {
            rewardActivityLpCode = program.getCode();
            List<String> activities = ApplicationDao.getProgramActivities(jdbcTemplate, rewardActivityLpCode);
            model.addAttribute("activities", activities);
            Activity activity1 = new Activity();
            model.addAttribute("rewardActivity", activity1);
        } else {
            //perform activity
            String customer = ApplicationDao.getLoggedInCustomerOrBrand(jdbcTemplate);
            String walletId = ApplicationDao.getWalletId(jdbcTemplate, customer);

            Wallet wallet = new Wallet();
            wallet.setWalletId(walletId);
            wallet.setLpCode(rewardActivityLpCode);
            wallet.setCategory(WalletCategory.EARN.name());
            wallet.setActivityName(activity.getActivityName());
            System.out.println(wallet);
            ApplicationDao.performCustomerTransaction(jdbcTemplate, wallet);
            return "redirect:/customer/reward_activities?success";
        }
        return "customer_reward_activities";
    }

    @GetMapping("/view_wallet")
    public String viewWalletInfo(Model model) {

        String customer = ApplicationDao.getLoggedInCustomerOrBrand(jdbcTemplate);
        List<Wallet> list = ApplicationDao.viewWalletData(jdbcTemplate, customer);
        model.addAttribute("transactions", list);
        return "customer_wallet";
    }

    @GetMapping("/redeem_points")
    public String redeem(Model model) {
        String customer = ApplicationDao.getLoggedInCustomerOrBrand(jdbcTemplate);
        List<LoyaltyProgram> programs = ApplicationDao.getCustomerEnrolledPrograms(jdbcTemplate, customer);
        model.addAttribute("programs", programs);
        model.addAttribute("loyaltyProgram", new LoyaltyProgram());
        return "customer_redeem_points";
    }

    @PostMapping("/redeem_points")
    public String redeemPoints(@ModelAttribute("loyaltyProgram") LoyaltyProgram program,
                               @ModelAttribute("reward") Reward reward, Model model) {

        if (reward == null || reward.getRewardName() == null) {
            List<String> activities = ApplicationDao.getProgramRewards(jdbcTemplate, program.getCode());
            model.addAttribute("activities", activities);
            Reward activity1 = new Reward();
            activity1.setLpCode(program.getCode());
            model.addAttribute("reward", activity1);
        } else {
            //perform activity
            String customer = ApplicationDao.getLoggedInCustomerOrBrand(jdbcTemplate);
            String walletId = ApplicationDao.getWalletId(jdbcTemplate, customer);
            Wallet wallet = new Wallet();
            wallet.setWalletId(walletId);
            wallet.setLpCode(reward.getLpCode());
            wallet.setCategory(WalletCategory.REDEEM.name());
            wallet.setActivityName(reward.getRewardName());
            System.out.println("Redeem "+wallet);
            ApplicationDao.performCustomerTransaction(jdbcTemplate, wallet);
            return "redirect:customer/redeem_points?success";
        }

        return "customer_redeem_points";
    }
}
