package kopo.gagyeview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MainController {

    @GetMapping(value="/")
    public String mainPage() {

        log.info("{}.mainPage Start!", this.getClass().getName());

        log.info("{}.mainPage End!", this.getClass().getName());

        return "index";
    }

}
