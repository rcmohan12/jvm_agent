package agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MyTransformer implements ClassFileTransformer {

	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
							ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;

		//Add instrumentation to Sample class alone
//		if (className.equals("myapp/Sample")) {?
		try {
			ClassPool classPool = ClassPool.getDefault();
			CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
			CtMethod[] methods = ctClass.getDeclaredMethods();

			if (!ctClass.isInterface() && ctClass.getPackageName().contains("myapp")) {
				for (CtMethod method : methods) {
					String[] cName = ctClass.getName().split("\\.");
					String callerClassName = "Thread.currentThread().getStackTrace()[2].getClassName()";
					if (method.getName().equalsIgnoreCase("main")) {
						callerClassName = "Thread.currentThread().getStackTrace()[1].getClassName()";
					}

					method.insertBefore("System.out.println(" + callerClassName + " + \" -> " + cName[cName.length - 1] + " : " + method.getName() + " \");");
					method.insertAfter("System.out.println(" + callerClassName + " + \" <-- " + cName[cName.length - 1] + " : " + method.getName() + " \");");

//						method.insertAfter("System.out.println(\" "+cName[cName.length-1]+" <-- \"+ " + callerClassName + " + \" " + " : " + method.getName() + "\");");
				}
				byteCode = ctClass.toBytecode();
				ctClass.detach();
			}

		} catch (Throwable ex) {
			System.out.println("Exception: " + ex);
			ex.printStackTrace();
		}
//		}
		return byteCode;
	}
}
