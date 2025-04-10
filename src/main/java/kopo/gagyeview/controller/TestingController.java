package kopo.gagyeview.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class TestingController {

    @GetMapping(value = "/test/index")
    public String mainPage(HttpSession session) {

        log.info("{}.mainPage Start!", this.getClass().getName());
        session.setAttribute("SS_USER_ID", "Tyson");
        session.setAttribute("SS_USER_NAME", "김태승");
        log.info("{}.mainPage End!", this.getClass().getName());

        return "test/index";
    }

}
