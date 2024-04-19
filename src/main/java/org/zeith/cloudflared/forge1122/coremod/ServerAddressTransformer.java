package org.zeith.cloudflared.forge1122.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class ServerAddressTransformer
		implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(!transformedName.equals("net.minecraft.client.multiplayer.ServerAddress")) return basicClass;
		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		processServerAddress(classNode);
		ClassWriter writer = new ClassWriter(3);
		classNode.accept(writer);
		return writer.toByteArray();
	}
	
	private void processServerAddress(ClassNode node)
	{
		CloudflaredCoremod1122.LOG.info("Transforming ServerAddress");
		
		String cSign = "L" + node.name + ";";
		for(MethodNode method : node.methods)
		{
			if(method.desc.equals("(Ljava/lang/String;)" + cSign) && (method.name.equals("a") || method.name.equals("fromString")))
				patchFromString(method);
		}
	}
	
	private void patchFromString(MethodNode node)
	{
		CloudflaredCoremod1122.LOG.info(" - Patching ServerAddress.fromString");
		
		LabelNode l1 = new LabelNode();
		LabelNode l2 = new LabelNode();
		
		InsnList insn = new InsnList();
		insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/zeith/cloudflared/forge1122/proxy/ClientProxy1122", "decodeAddress", node.desc, false));
		insn.add(new VarInsnNode(Opcodes.ASTORE, 1));
		insn.add(l1);
		insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
		insn.add(new JumpInsnNode(Opcodes.IFNULL, l2));
		insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
		insn.add(new InsnNode(Opcodes.ARETURN));
		insn.add(l2);
		
		node.instructions.insert(insn);
	}
}