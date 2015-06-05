package snails.common.jdbc.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.mvc.Scope.RenderArgs;
import play.templates.JavaExtensions;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.utils.Java;
import play.vfs.VirtualFile;
import snails.common.jdbc.exception.JDBCException;

public class JdbcUtil { 
    public final static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

    public static void invokeMethod(Object obj, Map params) {
        Set<Field> fieds = new HashSet<Field>();
        // 获取所有字段
        Java.findAllFields(obj.getClass(), fieds);

        for (Field field : fieds) {
            for (Iterator<Map.Entry<String, Object>> i = params.entrySet().iterator(); i.hasNext();) {
                Entry<String, Object> next = i.next();

                if (next.getKey().toString().equals(field.getName())) {
                    invokeSet(obj, field, next.getValue());
                }
            }
        }
    }
    
    public static Object invokeGet(Object obj, Field field) {
        Class objClazz = obj.getClass();

        try {
            // 不is开头并且是boolean的特殊处理
            if (field.getName().indexOf("is") < 0
                    && (boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType()))) {
                try {
                    if (field.getName().contains("is")) {
                        return objClazz.getMethod(field.getName()).invoke(obj);
                    }
                    return objClazz.getMethod("is" + JavaExtensions.capFirst(field.getName())).invoke(obj);
                } catch (Exception e1) {
                }
            }

            return objClazz.getMethod("get" + JavaExtensions.capFirst(field.getName())).invoke(obj);
        } catch (Exception e) {
            try {
                return field.get(obj);
            } catch (Exception e1) {
                log.error(e.getMessage(), e1);
                throw new JDBCException(e);
            }
        }
    }

    public static void invokeSet(Object obj, Field field, Object fieldValue) {
        Class objClazz = obj.getClass();

        try {
            // 不is开头并且是boolean的特殊处理
            if (field.getName().indexOf("is") < 0
                    && (boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType()))) {
                try {
                    objClazz.getMethod("set" + JavaExtensions.capFirst(field.getName().replace("is", "")), field.getType()).invoke(obj,fieldValue);
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            
            objClazz.getMethod("set" + JavaExtensions.capFirst(field.getName()), field.getType()).invoke(obj,
                    fieldValue);
        } catch (Exception e) {
            try {
                field.set(objClazz, fieldValue);
            } catch (Exception e1) {
                log.error(e.getMessage(), e);
                throw new JDBCException(e);
            }
        }
    }

    public static String renderResult(String _templateName,Map _renderParam,VirtualFile _virtualFolder){
        if(_virtualFolder == null || !_virtualFolder.getRealFile().exists()){
            log.error("vf is not exists");
            return "";
        }
        VirtualFile virtualFile = _virtualFolder.child(_templateName);
        if (virtualFile.exists()) {
            RenderArgs templateBinding = new RenderArgs();

            if (_renderParam != null && _renderParam.size()>0) {
                templateBinding.data.putAll(_renderParam);
            }

            Template template = TemplateLoader.load(virtualFile);

            if(template ==null){
                log.error("找不到模板文件");
                return "";
            }
            return template.render(templateBinding.data);
        }
        return "";
    }
    public static String renderResult(String _virtualFileName,Map _renderParam){
        for (VirtualFile virtualFolder : Play.templatesPath) {
            if (virtualFolder == null) {
                continue;
            }
            String result = renderResult(_virtualFileName,_renderParam,virtualFolder);
            if(StringUtils.isNotEmpty(result)){
                return result;
            }
        }
        log.error("找不到文件目录");
        return "";
    }
    
    private static String machineName =getMachineName();
    public static String getMachineName() {
        if(StringUtils.isNotEmpty(machineName)){
            log.trace("getMachineName = {} ",machineName);
            return machineName;
        }
        try {
            machineName =  InetAddress.getLocalHost().getHostName();
            log.trace("getMachineName = {} ",machineName);
            return machineName;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if(StringUtils.isEmpty(machineName)){
            System.out.println("ERROR:INIT MACHINE NAME ERROR");
            System.exit(1);
        }
        return null;
    }
    @SuppressWarnings("resource")
    public static List<Class> collectionClazz(String jarPath, Class toGet,final String pkgName) throws IOException, ClassNotFoundException {
        List<Class> clazzs = new ArrayList<Class>();
        File file = new File(jarPath);
        File[] jars = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(pkgName);
            }
        });
        if(jars == null || jars.length==0){
            log.warn("没有找到JAR包");
        }
        for (File jar: jars) {
            JarFile jarFile = new JarFile(jar);
            Enumeration<JarEntry> em = jarFile.entries();

            while (em.hasMoreElements()) {
                JarEntry entry = em.nextElement();

                String clazzName = entry.getName();
                if ((clazzName.indexOf(".class") != (clazzName.length() - 6)) || (clazzName.indexOf("$") > -1) || clazzName.indexOf(".class")<0) {
                    continue;
                }
                clazzName = clazzName.substring(0, clazzName.indexOf("."));
                clazzName = clazzName.replace("/", ".");
                Class clazz = null;
                try {
                    clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                }
                if(clazz !=null &&  toGet.isAssignableFrom(clazz)){
                    clazzs.add(clazz);
                }
            }
          
        }
        return clazzs;
    }
}
