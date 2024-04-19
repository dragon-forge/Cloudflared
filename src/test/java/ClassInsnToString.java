import com.zeitheron.hammercore.lib.zlib.json.JSONObject;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class ClassInsnToString
{
	public static void main(String[] args)
			throws IOException
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(Files.readAllBytes(new File("run/test.class").toPath()));
		classReader.accept(classNode, 0);
		toString(classNode, null);
	}
	
	public static void toString(ClassNode classNode, MethodNode $)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ClassBuilder builder = ObjectWebASM.classBuilder(\"name.to.ur.Cls\");\n\n");
		builder.append("builder.extendsClass(" + JSONObject.quote(classNode.superName).replaceAll("/", ".") + ");\n");
		Iterator var3 = classNode.interfaces.iterator();
		
		while(var3.hasNext())
		{
			String implement = (String) var3.next();
			builder.append("builder.implementsClass(" + JSONObject.quote(implement).replaceAll("/", ".") + ");\n");
		}
		
		{
			String val;
			FieldNode node;
			for(var3 = classNode.fields.iterator(); var3.hasNext(); builder.append("\nbuilder.field(new FieldNode(" + node.access + ", " + JSONObject.quote(node.name) + ", " + JSONObject.quote(node.desc) + ", " + JSONObject.quote(node.signature) + ", " + val + "));"))
			{
				node = (FieldNode) var3.next();
				val = "null";
				if(node.value instanceof String)
				{
					val = JSONObject.quote(node.value.toString());
				} else
				{
					val = Objects.toString(node.value);
				}
			}
		}
		
		builder.append("\n");
		var3 = classNode.methods.iterator();
		
		while(true)
		{
			MethodNode node;
			do
			{
				if(!var3.hasNext())
				{
					builder.append("\nClass<?> generatedClass = builder.buildClass();");
					System.out.println(builder.toString());
					return;
				}
				
				node = (MethodNode) var3.next();
			} while($ != null && node != $);
			
			Map<Label, String> nodes = new HashMap();
			String mnodes = "new MethodNode(" + node.access + ", " + JSONObject.quote(node.name) + ", " + JSONObject.quote(node.desc) + ", " + JSONObject.quote(node.signature) + ", new String" + (node.exceptions.isEmpty() ? "[0]" : "[]" + node.exceptions.toString().replaceAll("\\[", "{").replaceAll("]", "}")) + ")";
			builder.append("\nbuilder.method(" + mnodes + ", node ->\n{");
			Consumer<String> toList = (ln) ->
			{
				builder.append("\n\tinsn.add(" + ln + ");");
			};
			builder.append("\n\tInsnList insn = node.instructions;");
			AbstractInsnNode[] var8 = node.instructions.toArray();
			int var9 = var8.length;
			
			int var10;
			AbstractInsnNode i;
			for(var10 = 0; var10 < var9; ++var10)
			{
				i = var8[var10];
				if(i instanceof LabelNode)
				{
					toString(classNode, i, nodes, toList, (ln) ->
					{
						builder.append("\n\t" + ln);
					}, true);
				}
			}
			
			var8 = node.instructions.toArray();
			var9 = var8.length;
			
			for(var10 = 0; var10 < var9; ++var10)
			{
				i = var8[var10];
				toString(classNode, i, nodes, toList, (ln) ->
				{
					builder.append("\n\t" + ln);
				}, false);
			}
			
			builder.append("\n});\n");
		}
	}
	
	public static void toString(ClassNode classNode, AbstractInsnNode node, Map<Label, String> nodes, Consumer<String> toList, Consumer<String> append, boolean prerun)
	{
		String var;
		if(node instanceof LabelNode)
		{
			Label lbl = ((LabelNode) node).getLabel();
			var = nodes.containsKey(lbl) ? (String) nodes.get(lbl) : "l" + nodes.size();
			if(prerun)
			{
				append.accept("LabelNode " + var + " = new LabelNode();");
			} else
			{
				toList.accept(var);
			}
			
			nodes.put(lbl, var);
		} else if(node instanceof LineNumberNode)
		{
			LineNumberNode nd = (LineNumberNode) node;
			var = (String) nodes.get(nd.start.getLabel());
			toList.accept("new LineNumberNode(" + nd.line + ", " + (var == null ? "new LabelNode()" : var) + ")");
		} else if(node instanceof MethodInsnNode)
		{
			MethodInsnNode nd = (MethodInsnNode) node;
			toList.accept("new MethodInsnNode(" + opcodeName(nd.getOpcode()) + ", \"" + nd.owner + "\", \"" + nd.name + "\", \"" + nd.desc + "\")");
		} else if(node instanceof VarInsnNode)
		{
			VarInsnNode nd = (VarInsnNode) node;
			toList.accept("new VarInsnNode(" + opcodeName(nd.getOpcode()) + ", " + nd.var + ")");
		} else if(node instanceof TypeInsnNode)
		{
			TypeInsnNode nd = (TypeInsnNode) node;
			toList.accept("new TypeInsnNode(" + opcodeName(nd.getOpcode()) + ", \"" + nd.desc + "\")");
		} else if(node instanceof LdcInsnNode)
		{
			LdcInsnNode nd = (LdcInsnNode) node;
			Object tostr = nd.cst;
			String str = tostr.toString();
			if(tostr instanceof Type)
			{
				Type type = (Type) tostr;
				str = type.toString();
			} else if(tostr instanceof String)
			{
				str = JSONObject.quote(tostr.toString());
			}
			
			toList.accept("new LdcInsnNode(" + str + ")");
		} else if(node instanceof IntInsnNode)
		{
			IntInsnNode nd = (IntInsnNode) node;
			toList.accept("new IntInsnNode(" + opcodeName(nd.getOpcode()) + ", " + nd.operand + ")");
		} else if(node instanceof FieldInsnNode)
		{
			FieldInsnNode nd = (FieldInsnNode) node;
			var = nd.owner.compareTo(classNode.name) == 0 ? "builder.cname()" : JSONObject.quote(nd.owner);
			toList.accept("new FieldInsnNode(" + opcodeName(nd.getOpcode()) + ", " + var + ", " + JSONObject.quote(nd.name) + "," + JSONObject.quote(nd.desc) + ")");
		} else if(node instanceof InsnNode)
		{
			InsnNode nd = (InsnNode) node;
			toList.accept("new InsnNode(" + opcodeName(nd.getOpcode()) + ")");
		} else if(node instanceof JumpInsnNode)
		{
			JumpInsnNode nd = (JumpInsnNode) node;
			var = (String) nodes.get(nd.label.getLabel());
			toList.accept("new JumpInsnNode(" + opcodeName(nd.getOpcode()) + ", " + (var == null ? "new LabelNode()" : var) + ")");
		} else
		{
			append.accept("// Failed to parse: " + node.toString() + " @OP " + opcodeName(node.getOpcode()));
		}
		
	}
	
	public static String opcodeName(int opcode)
	{
		Field[] var1 = Opcodes.class.getDeclaredFields();
		int var2 = var1.length;
		
		for(int var3 = 0; var3 < var2; ++var3)
		{
			Field f = var1[var3];
			if(Modifier.isStatic(f.getModifiers()) && Integer.TYPE.isAssignableFrom(f.getType()))
			{
				try
				{
					if(f.getInt((Object) null) == opcode)
					{
						return "Opcodes." + f.getName();
					}
				} catch(IllegalAccessException | IllegalArgumentException var6)
				{
					Exception e = var6;
					e.printStackTrace();
				}
			}
		}
		
		return "0x" + Integer.toHexString(opcode);
	}
}