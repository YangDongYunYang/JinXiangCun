package com.ydy.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ydy.common.core.controller.BaseController;
import com.ydy.common.core.domain.AjaxResult;
import com.ydy.common.core.domain.model.RegisterBody;
import com.ydy.common.utils.StringUtils;
import com.ydy.framework.web.service.SysRegisterService;

/**
 * 注册验证
 *
 * @author ydy
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
