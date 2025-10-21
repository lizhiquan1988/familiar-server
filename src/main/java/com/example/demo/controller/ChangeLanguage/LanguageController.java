package com.example.demo.controller.ChangeLanguage;

import com.example.demo.Component.GlobalState;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/language")
public class LanguageController {

    private final GlobalState globalState;

    public LanguageController(GlobalState globalState) {
        this.globalState = globalState;
    }

    /**
     * 切换语言
     * @param languageCode 0=中文, 1=日文
     */
    @PostMapping("/change")
    public Map<String, Object> changeLanguage(@RequestParam int languageCode) {
        globalState.setValue(languageCode);
        return Map.of(
                "status", "ok",
                "message", "语言已切换为：" + (languageCode == 0 ? "中文" : "日文"),
                "languageCode", languageCode
        );
    }

    /**
     * 查询当前语言状态
     */
    @GetMapping("/current")
    public Map<String, Object> getLanguage() {
        return Map.of(
                "languageCode", globalState.getValue(),
                "languageName", globalState.getValue() == 0 ? "中文" : "日文"
        );
    }
}
