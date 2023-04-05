package ma.crud;


import com.github.tobato.fastdfs.conn.FdfsConnectionPool;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import ma.crud.UserService;
import ma.crud.cc.Employee;
import ma.crud.cc.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @RequestMapping("/emps")
    public String getUsers(Map<String, Object> map, Locale locale, Integer pageNo, Integer pageSize) {

        //int a = 1/0;

        Page<Employee> page = new Page<Employee>();
        if (pageNo == null || pageNo == 0){
            page.setPageNo(1);
        }else {
            page.setPageNo(pageNo);
        }

        if (pageSize == null || pageSize == 0){
            page.setPageSize(3);
        }else{
            page.setPageSize(pageSize);
        }

        userService.getCount(page);
        map.put("locale",locale.toString());
        map.put("page", page);
        return "list";
    }



    @RequestMapping(value = "/emp", method = RequestMethod.GET)
    public String addUserShow(Map<String, Object> map) {
        Map<String, String> genders = new HashMap<String, String>();
        genders.put("1", "男");
        genders.put("0", "女");
        map.put("genders", genders);
        map.put("departments",userService.getDepartment());
        map.put("employee", new Employee());
        return "input";
    }


    @RequestMapping(value = "/emp", method = RequestMethod.POST)
    public String save(Employee employee, BindingResult result, Map<String, Object> map, @RequestParam("head") MultipartFile head) {

        if (result.getErrorCount() > 0) {
            System.out.println("114514");

            for (FieldError error : result.getFieldErrors()) {
                System.out.println(error.getField() + ":" + error.getDefaultMessage());
            }
            Map<String, String> genders = new HashMap<String, String>();
            genders.put("1", "男");
            genders.put("0", "女");

            map.put("departments",userService.getDepartment());
            map.put("genders", genders);
            return "input";
        }


        if (head != null || !head.equals(""))//head不等于null 或者不等于空 就可以进来
        {
            // 初始化配置文件
            try {
            String fiel = head.getOriginalFilename();//获取文件
            System.out.println(head.getOriginalFilename());
            String[] fenge = fiel.split("\\.");//根据我的文件名来用\\.分割
            StorePath storePath = fastFileStorageClient.uploadFile(null, head.getInputStream(), head.getSize(), fenge[fenge.length - 1]);
            System.out.println(storePath);

            employee.setGroup(storePath.getGroup());//获取到组
            employee.setHeadUrl(storePath.getPath());//获取到路径
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        userService.addUser(employee);
        return "redirect:/emps";
    }

    @RequestMapping("/headshow")
    public void test(HttpServletResponse response, String headUrl, String group)
    {

        if(headUrl != null || !headUrl.equals(""))
        {
            try {
                byte[] b = fastFileStorageClient.downloadFile(group,headUrl,new DownloadByteArray());//先组在路径
                response.getOutputStream().write(b, 0, b.length);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    @RequestMapping(value="/emp/{id}", method=RequestMethod.DELETE)
    public String delete(@PathVariable("id") Integer id){

        Employee employees = userService.getUserById(id);
        String path = employees.getHeadUrl();

        if (path != null && !path.equals("")) { //路径不等于null 或者不等于空就能进来
            File file = new File(path);
            file.delete();
        }

        try {
            employees.getGroup();
            fastFileStorageClient.deleteFile(employees.getGroup(),employees.getHeadUrl());
        }catch (Exception e){
            e.printStackTrace();
        }
        employees.setId(id);
        userService.deleteUser(employees);
        return "redirect:/emps";
    }

    //修改
    @RequestMapping(value = "/emp", method = RequestMethod.PUT)
    public String update(@Valid Employee employee, BindingResult result, Map<String, Object> map, @RequestParam("head") MultipartFile file) {

        System.out.println("put------------");
        if(result.getErrorCount() > 0){
            System.out.println("11451419191");

            for(FieldError error:result.getFieldErrors()){
                System.out.println(error.getField() + ":" + error.getDefaultMessage());
            }
            Map<String, String> genders = new HashMap<String, String>();
            genders.put("1", "男");
            genders.put("0", "女");

            map.put("departments",userService.getDepartment());
            map.put("genders", genders);

            return "input";
        }
//删除图片
        fastFileStorageClient.deleteFile(employee.getGroup(), employee.getHeadUrl());
        //file.getOriginalFilename()是得到上传时的文件名
        String Filename = file.getOriginalFilename();
        //分隔符  是转意符
        String[] fenge = Filename.split("\\.");
        try {
            StorePath storePath  = fastFileStorageClient.uploadFile(null, file.getInputStream(), file.getSize(),fenge[fenge.length - 1]);
            //新增把头像图片新增的路径给emp对象的字段
            employee.setHeadUrl(storePath.getPath());
            employee.setGroup(storePath.getGroup());
        } catch (IOException e) {
            e.printStackTrace();
        }
     /*   Employee employees = userService.getUserById(employee.getId());
        String path = employees.getHeadUrl();

        if (employees.getHeadUrl() == null || employees.equals("")) {


            String fileName = head.getOriginalFilename();
            String fileNames[] = fileName.split("\\.");

            long a = 0;
            synchronized ("a") {
                a = System.currentTimeMillis();
            }

            String uploadName = a +"."+ fileNames[fileNames.length - 1];
            path ="E:\\Test\\"+uploadName;
        }
        try {
            InputStream inputStream = head.getInputStream();// 获取到文件上传的流
            OutputStream outputStream = new FileOutputStream(path);

            byte[] b = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, length);
                outputStream.flush();
            }

            outputStream.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
        employee.setHeadUrl(path);*/
        userService.updateUser(employee);
        System.out.println(employee);
        return "redirect:/emps";
    }

    @RequestMapping(value="/emp/{id}", method=RequestMethod.GET)
    public String update(@PathVariable("id") Integer id,Map<String, Object> map){

        Map<String, String> genders = new HashMap<>();
        genders.put("1", "男");
        genders.put("0", "女");
        map.put("genders", genders);
        map.put("departments",userService.getDepartment());
        map.put("employee", userService.getUserById(id));
        return "input";

    }

    @ModelAttribute
    public void getEmployee(@RequestParam(value="id",required=false) Integer id,Map<String, Object> map){
        if(id != null){
            map.put("employee", userService.getUserById(id));
        }
    }

}













