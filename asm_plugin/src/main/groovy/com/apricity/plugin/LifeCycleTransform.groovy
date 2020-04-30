package com.apricity.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.utils.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import javax.xml.crypto.dsig.TransformException
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public class LifeCycleTransform extends Transform {

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println "---------LifeCyclePlugin start-----------"
        Collection<TransformInput> inputCollections = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        // 删除之前的输出
        if (outputProvider!=null)
            outputProvider.deleteAll()
        inputCollections.each {TransformInput transformInput->
            transformInput.directoryInputs.each {
                DirectoryInput directoryInput->
                    handleDirectoryInput(directoryInput, outputProvider)
            }
            //遍历jarInputs
//            transformInput.jarInputs.each { JarInput jarInput ->
//                //处理jarInputs
//                handleJarInputs(jarInput, outputProvider)
//            }
        }
    }

    private void handleDirectoryInput( DirectoryInput directoryInput, TransformOutputProvider outputProvider){

        if (directoryInput.file.isDirectory()) {
            //列出目录所有文件（包含子文件夹，子文件夹内文件）
            directoryInput.file.eachFileRecurse { File file ->
                def name = file.name
                println "find class = "+name
                if (name.endsWith(".class") && !name.startsWith("R\$")
                        && "R.class" != name && "BuildConfig.class" != name) {
                    println '----------- deal with "class" file <' + name + '> -----------'
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new LifeCycleVisitor(classWriter)
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(
                            file.parentFile.absolutePath + File.separator + name)
                    fos.write(code)
                    fos.close()
                }
            }
        }
//        File dir = directoryInput.file
//        if (dir){
//            dir.traverse(type: FileType.FILES, nameFilter:~/.*\.class/){
//                File file->
//                    println "find class = "+file.name
//                    // 对class文件进行读取和解析
//                    ClassReader classReader = new ClassReader(file.bytes)
//                    // 对class写入
//                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                    // 访问class文件的相应内容，解析到某一结构会通知ClassVisitor的相应方法
//                    ClassVisitor classVisitor = new LifeCycleVisitor(classWriter)
//                    // 一次调用ClassVisitor的各个方法
//                    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
//                    // 将最终修改的字节码以byte数组形式返回
//                    byte[] bytes = classWriter.toByteArray()
//                    // 写入到原路径
//                    FileOutputStream fileOutputStream = new FileOutputStream(file.parentFile.absolutePath + File.separator + name)
//                    fileOutputStream.write(bytes)
//                    fileOutputStream.close()
//            }
//        }
        // 处理完输入文件后把输出传给下一个文件
        def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    /**
     * 处理Jar中的class文件
     */
    private void handleJarInputs(JarInput jarInput, TransformOutputProvider outputProvider) {
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            //重名名输出文件,因为可能同名,会覆盖
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            //避免上次的缓存被重复插入
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
            //用于保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                //插桩class
                if (entryName.endsWith(".class") && !entryName.startsWith("R\$")
                        && !"R.class".equals(entryName) && !"BuildConfig.class".equals(entryName)
                        ) {
                    //class文件处理
                    println '----------- deal with "jar" class file <' + entryName + '> -----------'
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new LifeCycleVisitor(classWriter)
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            //结束
            jarOutputStream.close()
            jarFile.close()
            def dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
        }
    }
    /**
     * Task:app:transformClassesWithASMLifeCycleForDebug
     * @return
     */
    @Override
    String getName() {
        return "ASMLifeCycle"
    }

    /**
     * 设置接收文件类型
     *  ContentType:
     *      CLASSES
     *      RESOURCES
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 检索范围
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    /**
     * 是否支持增量模式
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }
}