package com.nowcoder.community.controller;


import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }

        //获取文件名后缀并判断
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;

        //确定文件存放的路径
        File dest = new File(uploadPath + '/' + fileName);

        //存储文件
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        // 更新当前用户头像路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 服务器存放路径
        fileName = uploadPath + '/' + fileName;
        // 获取文件名后缀
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        // 响应图片
        // 图片是二进制数据，要用到字节流
        // 图片响应类型默认是"image/" + 后缀
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName); // 相当于后面加final，final中自动执行fis.close()方法
                OutputStream os = response.getOutputStream(); //SspringMVC中管理输出流，会自动关闭，但输入流是自定义的，需要手动关闭
            )
        {
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){ // 等于-1就表示没读到数据
                os.write(buffer, 0, b); // 从零开始读到b
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String originalPassword, String newPassword, String newPasswordAgain){
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updataPassword(user.getId(), originalPassword, newPassword, newPasswordAgain);

        if(map.containsKey("originalPasswordMsg")){
            model.addAttribute("originalPasswordMsg", map.get("originalPasswordMsg"));
            return "/site/setting";
        }
        if(map.containsKey("newPasswordMsg")){
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
        return "redirect:/index";
    }

}
