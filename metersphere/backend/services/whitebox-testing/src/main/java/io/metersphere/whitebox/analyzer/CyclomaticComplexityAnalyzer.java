package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.ComplexityViolation;
import org.springframework.stereotype.Component;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import io.metersphere.whitebox.tester.ServiceUrlConfig;
import java.util.List;

@Component
public class CyclomaticComplexityAnalyzer {
    
    @Autowired
    private ServiceUrlConfig serviceUrlConfig;
    
    public List<ComplexityViolation> analyzeServiceComplexity(String serviceName) {
        List<ComplexityViolation> violations = new ArrayList<>();
        
        try {
            // 获取服务类路径
            String servicePath = getServiceClassPath(serviceName);
            
            // 遍历服务目录下的所有类文件
            Path serviceDir = Paths.get(servicePath);
            if (Files.exists(serviceDir)) {
                Files.walk(serviceDir)
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        try {
                            analyzeClassFile(path.toString(), violations);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return violations;
    }
    
    private void analyzeClassFile(String classFilePath, List<ComplexityViolation> violations) throws Exception {
        try (InputStream inputStream = new FileInputStream(classFilePath)) {
            ClassReader classReader = new ClassReader(inputStream);
            ClassVisitor classVisitor = new ComplexityClassVisitor(violations);
            classReader.accept(classVisitor, 0);
        }
    }
    
    private String getServiceClassPath(String serviceName) {
        // 从配置中获取服务类路径，如果没有配置则使用默认路径
        if (serviceName == null || serviceName.isEmpty()) {
            return "target/classes";
        }
        
        return serviceUrlConfig.getClassPathOrDefault(serviceName, "../" + serviceName + "/target/classes");
    }
    
    private static class ComplexityClassVisitor extends ClassVisitor {
        private List<ComplexityViolation> violations;
        private String className;
        
        public ComplexityClassVisitor(List<ComplexityViolation> violations) {
            super(Opcodes.ASM9);
            this.violations = violations;
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name.replace("/", ".");
            super.visit(version, access, name, signature, superName, interfaces);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new ComplexityMethodVisitor(mv, className, name, violations);
        }
    }
    
    private static class ComplexityMethodVisitor extends MethodVisitor {
        private String className;
        private String methodName;
        private List<ComplexityViolation> violations;
        private int complexity = 1; // 基础复杂度为1
        
        public ComplexityMethodVisitor(MethodVisitor mv, String className, String methodName, List<ComplexityViolation> violations) {
            super(Opcodes.ASM9, mv);
            this.className = className;
            this.methodName = methodName;
            this.violations = violations;
        }
        
        @Override
        public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
            // 分支指令增加复杂度
            if (opcode == Opcodes.IFEQ || opcode == Opcodes.IFNE || opcode == Opcodes.IFLT || 
                opcode == Opcodes.IFGE || opcode == Opcodes.IFGT || opcode == Opcodes.IFLE ||
                opcode == Opcodes.IF_ICMPEQ || opcode == Opcodes.IF_ICMPNE || opcode == Opcodes.IF_ICMPLT ||
                opcode == Opcodes.IF_ICMPGE || opcode == Opcodes.IF_ICMPGT || opcode == Opcodes.IF_ICMPLE ||
                opcode == Opcodes.IF_ACMPEQ || opcode == Opcodes.IF_ACMPNE || opcode == Opcodes.IFNULL ||
                opcode == Opcodes.IFNONNULL) {
                complexity++;
            }
            super.visitJumpInsn(opcode, label);
        }
        
        @Override
        public void visitLookupSwitchInsn(org.objectweb.asm.Label dflt, int[] keys, org.objectweb.asm.Label[] labels) {
            // Switch语句增加复杂度
            complexity += labels.length;
            super.visitLookupSwitchInsn(dflt, keys, labels);
        }
        
        @Override
        public void visitTableSwitchInsn(int min, int max, org.objectweb.asm.Label dflt, org.objectweb.asm.Label... labels) {
            // Switch语句增加复杂度
            complexity += labels.length;
            super.visitTableSwitchInsn(min, max, dflt, labels);
        }
        
        @Override
        public void visitEnd() {
            // 检查复杂度是否超过阈值
            if (complexity > 10) { // 阈值设为10
                ComplexityViolation violation = new ComplexityViolation();
                violation.setClassName(className);
                violation.setMethodName(methodName);
                violation.setComplexity(complexity);
                violation.setThreshold(10);
                violations.add(violation);
            }
            super.visitEnd();
        }
    }
}