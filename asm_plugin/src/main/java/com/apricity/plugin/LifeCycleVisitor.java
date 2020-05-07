package com.apricity.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LifeCycleVisitor extends ClassVisitor implements Opcodes{

    private String className;
    private String superName;

    public LifeCycleVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        this.superName = superName;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println("LifeCycleVisitor MethodVisitor---->name = [" + name + "], superName = [" + superName + "]");
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        if ("onClick".equals(name)){
            System.out.println("---->onClick");
            return new LifeCycleOnClickVisitor(methodVisitor);
        }
        if ("androidx/appcompat/app/AppCompatActivity".equals(superName)){
            if ("onCreate".equals(name)){
                System.out.println("---->onCreate");
                return new LifeCycleOnCreateVisitor(methodVisitor);
            }else if ("onResume".equals(name)){
                System.out.println("---->onResume");
                return new LifeCycleOnResumeVisitor(methodVisitor);
            }else if ("onPause".equals(name)){
                System.out.println("---->onPause");
                return new LifeCycleOnPauseVisitor(methodVisitor);
            }else if ("onDestroy".equals(name)){
                System.out.println("---->onDestroy");
                return new LifeCycleOnDestroyVisitor(methodVisitor);
            }
        }
        return methodVisitor;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
