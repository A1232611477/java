package com.fenglin.springboottest.web;

import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.UserService;
import com.fenglin.springboottest.web.form.LoginForm;
import com.fenglin.springboottest.web.form.RegisterForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 服务器端页面流程（Thymeleaf + Session）
 *
 * <p>页面跳转链路：</p>
 * <pre>
 *   首页 /            ──「注册」──▶ /register ──提交──▶ 成功 302 ▶ /login?...
 *                     ──「登录」──▶ /login    ──提交──▶ 成功 302 ▶ /home
 *                     ◀── 302 ── /logout  ◀──「退出」── /home
 *   未登录直接访问 /home ──拦截器 302──▶ /login?redirect=%2Fhome ──登录成功──▶ 原路跳回 /home
 * </pre>
 *
 * <p>和 {@code UserController}（教学演示，密码写死 123）、{@code AuthController}（REST + JWT）
 * 是并列的三套入口，服务的都是同一张 {@code t_user} 表（本类走 {@link UserService}）。</p>
 */
@Controller
public class PageController {

    /** 登录后默认落到用户主页 */
    private static final String DEFAULT_AFTER_LOGIN = "/home";

    private final UserService userService;

    public PageController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 首页 ====================

    @GetMapping("/")
    public String index() {
        return "site/index";
    }

    // ==================== 注册 ====================

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "site/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("form") RegisterForm form,
                             BindingResult binding,
                             RedirectAttributes ra) {
        // 「两次密码一致」是跨字段校验，注解表达不了，手动挂到确认密码框下面
        if (hasText(form.getPassword()) && !form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "mismatch", "两次输入的密码不一致");
        }
        if (binding.hasErrors()) {
            return "site/register";
        }

        String nickname = form.getNickname().trim();
        try {
            userService.register(nickname, form.getPassword());
        } catch (BizException e) {
            // 409 昵称重复 -> 挂到昵称框；其它（如数据库异常）-> 页面顶部横幅
            if (e.getCode() == 409) {
                binding.rejectValue("nickname", "duplicate", e.getMessage());
            } else {
                binding.reject("register", e.getMessage());
            }
            return "site/register";
        }

        // 注册成功 -> 302 跳登录页，并把昵称带过去预填
        ra.addFlashAttribute("notice", "注册成功，请使用新账号登录");
        ra.addFlashAttribute("noticeNickname", nickname);
        return "redirect:/login";
    }

    // ==================== 登录 ====================

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(required = false) String redirect) {
        if (!model.containsAttribute("form")) {
            LoginForm form = new LoginForm();
            // 刚注册完跳过来的话，把昵称预填进账号框
            Object lastName = model.getAttribute("noticeNickname");
            if (lastName instanceof String s) {
                form.setNickname(s);
            }
            model.addAttribute("form", form);
        }
        model.addAttribute("redirect", redirect);
        // 被拦截器「赶」过来的时候（带 redirect 参数），给一句解释
        model.addAttribute("needLogin", redirect != null && !redirect.isBlank());
        return "site/login";
    }

    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute("form") LoginForm form,
                          BindingResult binding,
                          @RequestParam(required = false) String redirect,
                          HttpServletRequest request,
                          Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("redirect", redirect);
            return "site/login";
        }

        User user;
        try {
            user = userService.authenticate(form.getNickname(), form.getPassword());
        } catch (BizException e) {
            // 401 账号或密码错误 / 403 账号已被禁用，统一挂到密码框下面
            binding.rejectValue("password", "auth", e.getMessage());
            model.addAttribute("redirect", redirect);
            return "site/login";
        }

        // 防会话固定攻击：登录成功先废掉旧 session，再换一个新的
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute(PageAuthInterceptor.SESSION_USER_ID, user.getId());

        // 302 跳转：优先回登录前想去的页面，否则去用户主页
        return "redirect:" + safeRedirect(redirect);
    }

    // ==================== 用户主页 ====================

    @GetMapping("/home")
    public String home(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(PageAuthInterceptor.SESSION_USER_ID);
        if (userId == null) {
            // 正常走不到这里（拦截器已经拦了），兜底防并发 / 手动清 session
            return "redirect:/login?redirect=" + DEFAULT_AFTER_LOGIN;
        }
        try {
            model.addAttribute("user", userService.getUserVO(userId));
        } catch (BizException e) {
            // 用户被删了：清掉登录态，回登录页并提示
            session.invalidate();
            return "redirect:/login?expired";
        }
        return "site/home";
    }

    // ==================== 退出登录 ====================

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout";
    }

    // ==================== 工具 ====================

    /** 只允许跳回本站内部路径，避免 ?redirect=//evil.com 这种开放重定向 */
    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return DEFAULT_AFTER_LOGIN;
        }
        String target = redirect.trim();
        if (target.startsWith("//") || !target.startsWith("/")) {
            return DEFAULT_AFTER_LOGIN;
        }
        return target;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
