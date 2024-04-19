package org.zeith.cloudflared.forge1122.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class HttpUtilTransformer
		implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(!transformedName.equals("net.minecraft.util.HttpUtil")) return basicClass;
		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		processHttpUtil(classNode);
		ClassWriter writer = new ClassWriter(3);
		classNode.accept(writer);
		return writer.toByteArray();
	}
	
	private void processHttpUtil(ClassNode node)
	{
		CloudflaredCoremod1122.LOG.info("Transforming HttpUtil");
		
		for(MethodNode method : node.methods)
		{
			if(method.desc.equals("()I") && (method.name.equals("a") || method.name.equals("getSuitableLanPort")))
				patchGetSuitableLanPort(method);
		}
	}
	
	private void patchGetSuitableLanPort(MethodNode node)
	{
		CloudflaredCoremod1122.LOG.info(" - Patching HttpUtil.getSuitableLanPort");
		
		LabelNode l1 = new LabelNode();
		LabelNode l2 = new LabelNode();
		
		InsnList insn = new InsnList();
		insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/zeith/cloudflared/forge1122/proxy/ClientProxy1122", "pickPort", "()Ljava/lang/Integer;", false));
		insn.add(new VarInsnNode(Opcodes.ASTORE, 0));
		insn.add(l1);
		insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insn.add(new JumpInsnNode(Opcodes.IFNULL, l2));
		insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
		insn.add(new InsnNode(Opcodes.IRETURN));
		insn.add(l2);
		
		node.instructions.insert(insn);
	}
}