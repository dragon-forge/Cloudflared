package org.zeith.cloudflared.forge1122.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class IntegratedServerTransformer
		implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(!transformedName.equals("net.minecraft.server.integrated.IntegratedServer")) return basicClass;
		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		processIntegratedServer(classNode, !transformedName.equals(name));
		ClassWriter writer = new ClassWriter(3);
		classNode.accept(writer);
		return writer.toByteArray();
	}
	
	private void processIntegratedServer(ClassNode node, boolean obf)
	{
		CloudflaredCoremod1122.LOG.info("Transforming IntegratedServer");
		
		String GameType = "L" + (obf ? "ams" : "net/minecraft/world/GameType") + ";";
		String descriptor = "(" + GameType + "Z)Ljava/lang/String;";
		
		for(MethodNode method : node.methods)
		{
			if(method.desc.equals(descriptor) && (method.name.equals("a") || method.name.equals("shareToLAN")))
				patchShareToLAN(node.name, obf, method);
		}
	}
	
	private void patchShareToLAN(String IntegratedServer, boolean obf, MethodNode node)
	{
		CloudflaredCoremod1122.LOG.info(" - Patching IntegratedServer.shareToLAN");
		
		String NetworkSystem = obf ? "oz" : "net/minecraft/network/NetworkSystem";
		
		InsnList insn = new InsnList();
		
		insn.add(new VarInsnNode(Opcodes.ILOAD, 3));
		insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/zeith/cloudflared/forge1122/proxy/ClientProxy1122", "onSharedToLan", "(L" + IntegratedServer + ";I)V", false));
		insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
		
		node.instructions.insertBefore(findGetNetworkSystem(IntegratedServer, NetworkSystem, node.instructions), insn);
	}
	
	private AbstractInsnNode findGetNetworkSystem(String IntegratedServer, String NetworkSystem, InsnList insn)
	{
		ListIterator<AbstractInsnNode> itr = insn.iterator();
		while(itr.hasNext())
		{
			AbstractInsnNode next = itr.next();
			if(next instanceof MethodInsnNode && next.getOpcode() == Opcodes.INVOKEVIRTUAL)
			{
				MethodInsnNode minsn = (MethodInsnNode) next;
				if(minsn.owner.equals(IntegratedServer) && minsn.desc.equals("()L" + NetworkSystem + ";"))
				{
					return minsn;
				}
			}
		}
		throw new RuntimeException("Unable to find INVOKEVIRTUAL IntegratedServer/getNetworkSystem instruction node.");
	}
}