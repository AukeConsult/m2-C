package no.auke.p2p.m2.general;

import java.lang.reflect.Field;

import no.auke.p2p.m2.InitVar;
import no.auke.util.FileUtil;
import no.auke.util.StringConv;

public class ParamReader {
	public static void readIfExisting() {
		// write to m2.init
		if (!FileUtil.isFileExists("m2.config")) {
			write();
		} else {
			// read from outside
			try {
				Class<InitVar> initVarClass = InitVar.class;
				String content = StringConv.UTF8(FileUtil.getByteArrayFromFile("m2.init"));
				for (String line : content.split("\\r?\\n")) {
					if (line.trim().length() > 0 && !line.startsWith("//") && !line.startsWith("#")) {
						try {
							String[] keyvalue = line.split(":", 2);
							Field f = initVarClass.getField(keyvalue[0].trim());
							String val = keyvalue[1].trim();
							if (f.getType().equals(int.class)) {
								f.setInt(null, Integer.valueOf(val));
							} else if (f.getType() == boolean.class) {
								f.setBoolean(null, Boolean.valueOf(val));
							} else if (f.getType() == long.class) {
								f.setLong(null, Long.valueOf(val));
							} else if (f.getType() == short.class) {
								f.setShort(null, Short.valueOf(val));
							} else if (f.getType() == float.class) {
								f.setFloat(null, Float.valueOf(val));
							} else if (f.getType() == double.class) {
								f.setDouble(null, Double.valueOf(val));
							} else if (f.getType() == char.class) {
								f.setChar(null, val.charAt(0));
							} else if (f.getType() == byte.class) {
								f.setByte(null, Byte.valueOf(val));
							} else if (f.getType() == String.class) {
								f.set(null, val);
							} else {
								f.set(null, val);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}
				}
			} catch (Exception e) {}
		}
	}
	public static void write() {
		FileUtil.writeToFile("m2.config", getInitParamContent(), false);
	}
	public static byte[] getInitParamContent() {
		Class<InitVar> initVarClass = InitVar.class;
		StringBuilder builder = new StringBuilder();
		String line = System.getProperty("line.separator");
		for (Field f : initVarClass.getFields()) {
			try {
				if (f.isAnnotationPresent(Description.class)) {
					Description desc = (Description) f.getAnnotation(Description.class);
					if (desc.makeInit()) {
						String comment = desc.value();
						builder.append(comment);
						builder.append(line);
						builder.append("//" + f.getName() + " : " + String.valueOf(f.get(null)));
						builder.append(line);
					} else {
						continue;
					}
				}
			} catch (SecurityException e) {
				continue;
			} catch (Exception e) {
				continue;
			}
		}
		return StringConv.getBytes(builder.toString());
	}
}
