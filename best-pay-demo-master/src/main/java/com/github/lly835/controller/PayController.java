package com.github.lly835.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.lly835.config.WechatAccountConfig;
import com.github.lly835.util.HttpUtil;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import com.lly835.bestpay.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 支付相关
 */
@Controller
@Slf4j
public class PayController {
    Logger log=LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BestPayServiceImpl bestPayService;

    private static final String get_openid="https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

    @Autowired
    private WechatAccountConfig accountConfig;

    /**
     * 授权获取CODE
     * @return
     */
    @GetMapping(value = "/pay")
    public ModelAndView pay() {
        return new ModelAndView("authon.html");
    }

    /**
     * 发起支付
     * 通过code, 获取openid,
     */
    @GetMapping(value = "/goodspay")
    public ModelAndView goodspay(@RequestParam("code") String code,String money,
                            Map<String, Object> map) {
        String uri=String.format(get_openid,accountConfig.getMpAppId(),accountConfig.getSecret(),code);
        log.info("【获取openid】request={}", uri);
        String json=HttpUtil.get(uri);
        log.info("【获取openid】request={}", json);
        JSONObject jsonobj=JSONObject.parseObject(json);
        log.info("openid={}",jsonobj.get("openid"));
        String openid= (String) jsonobj.get("openid");
        return pay(openid,money,map);
    }


    /**
     * 发起支付
     */
    public ModelAndView pay(String openid,String money,
                            Map<String, Object> map) {
        PayRequest request = new PayRequest();
        Random random = new Random();

        //支付请求参数
        request.setPayTypeEnum(BestPayTypeEnum.WXPAY_H5);
        request.setOrderId(String.valueOf(random.nextInt(1000000000)));
        if(StringUtils.isNotEmpty(money)){
            request.setOrderAmount(new Double(money));
        }else{
            request.setOrderAmount(0.01);
        }
        request.setOrderName("支付测试");
        request.setOpenid(openid);
        log.info("【发起支付】request={}", JsonUtil.toJson(request));

        PayResponse payResponse = bestPayService.pay(request);
        log.info("【发起支付】response={}", JsonUtil.toJson(payResponse));

        map.put("payResponse", payResponse);

        return new ModelAndView("pay/create", map);
    }

    /**
     * 异步回调
     */
    @PostMapping(value = "/notify")
    public ModelAndView notify(@RequestBody String notifyData) throws Exception {
        log.info("【异步回调】request={}", notifyData);
        PayResponse response = bestPayService.asyncNotify(notifyData);
        log.info("【异步回调】response={}", JsonUtil.toJson(response));

        return new ModelAndView("pay/success");
    }


}
