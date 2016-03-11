package org.martial.hadoop.db;

import java.sql.SQLException;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.martial.hadoop.JobConfig;
import org.martial.hadoop.annotations.Sql;
import org.martial.hadoop.annotations.SqlOperation;
import org.martial.hadoop.enums.SQLTypes;
import org.martial.hadoop.utils.Errors;

public class JobDBClient {
	private static Logger LOG = LogManager.getLogger(JobDBClient.class);
	private String user;
	private String passWord;
	private String url;

	public JobDBClient(String user, String passWord, String url) {
		set(user, passWord, url);
	}

	public JobDBClient() {
		String userName = JobConfig.getValue("db.user");
		String pass = JobConfig.getValue("db.password");
		String dbUrl = JobConfig.getValue("db.url");
		set(userName, pass, dbUrl);
	}

	private void set(String user, String passWord, String url) {
		LOG.info(url);
		this.user = user;
		this.passWord = passWord;
		this.url = url;
	}

	@SuppressWarnings("unchecked")
	public <T> T instance(Class<T> interfaceClz) throws NotFoundException,
			ClassNotFoundException, CannotCompileException,
			InstantiationException, IllegalAccessException {
		T client = null;
		if (interfaceClz.isInterface()) {
			ClassPool pool = ClassPool.getDefault();
			pool.insertClassPath(new ClassClassPath(JobDBClient.class));
			String interfaceName = interfaceClz.getName();
			CtClass dao = pool.get(interfaceName);
			CtClass daoImpl = this.createImpl(pool, dao, interfaceName);
			CtClass se = pool.get(SQLException.class.getName());
			CtClass ex[] = { se };
			for (CtMethod cm : dao.getDeclaredMethods()) {
				Sql sql = (Sql) cm.getAnnotation(Sql.class);
				String sqlStr = sql.value();
				if (StringUtils.isNotBlank(sqlStr)) {
					CtClass[] paramtertypes = cm.getParameterTypes();
					CtMethod method = new CtMethod(cm.getReturnType(),
							cm.getName(), paramtertypes, daoImpl);
					SqlOperation type = (SqlOperation) cm
							.getAnnotation(SqlOperation.class);
					SQLTypes st = type.value();
					StringBuilder sb = new StringBuilder();
					StringBuilder ppsb = this.writePrep(paramtertypes);
					sb.append("{");
					sb.append("java.sql.Connection con=null;\n");
					sb.append("java.sql.PreparedStatement ps=null;\n");
					if (st.equals(SQLTypes.SELECT)) {
						this.writeQuery(sb, ppsb, cm, sqlStr);
					} else {
						this.writeExecute(sb, ppsb, sqlStr);
					}
					sb.append("}");
					// LOG.info(sb.toString());
					method.setBody(sb.toString());
					method.setExceptionTypes(ex);
					daoImpl.addMethod(method);
				} else {
					Errors.error("sql:" + sqlStr);
					throw new NullPointerException("sql注解为空！");
				}
			}
			client = (T) daoImpl.toClass().newInstance();
		} else {
			ClassFormatError cfe = new ClassFormatError("非接口类型无法初始化");
			Errors.error("类型错误:", cfe);
			throw cfe;
		}
		return client;
	}

	public void writeQuery(StringBuilder sb, StringBuilder ppsb,
			CtMethod method, String sql) throws NotFoundException {
		CtClass returnType = method.getReturnType();
		String rtName = returnType.getName();
		boolean isPrimitive = returnType.isPrimitive()
				|| rtName.endsWith("String");
		boolean isList = rtName.endsWith("List");
		if (isList) {
			sb.append("List<" + rtName + "> list=new ArrayList<" + rtName
					+ ">();\n");
		} else if (isPrimitive) {
			sb.append(rtName + " obj=0;\n");
		} else {
			sb.append(rtName + " obj=null;\n");
		}
		sb.append("java.sql.ResultSet rs=null;\n");
		sb.append("try {con = java.sql.DriverManager.getConnection(url, user, passWord);\n");
		sb.append("  	String sql = \"" + sql + "\";\n");
		sb.append("  	LOG.info(sql);\n");
		sb.append("  	ps = con.prepareStatement(sql);\n");
		sb.append(ppsb.toString());
		sb.append("		rs=ps.executeQuery();\n");
		sb.append("    while(rs.next()){\n");
		if (isPrimitive) {
			rtName = rtName.substring(0, 1).toUpperCase() + rtName.substring(1);
			sb.append("	obj=rs.get" + rtName + "(1);\n");
		} else if (isList) {
			sb.append("	" + rtName + " obj=new " + rtName + "();\n");
			sb.append("	list.add(obj.setValue(rs));\n");
		} else {
			sb.append("	obj=new " + rtName + "();\n");
			sb.append("	obj=obj.setValue(rs);\n");
		}
		sb.append("    }\n");
		sb.append("  } catch (Exception e) {\n");
		sb.append("    org.waps.jobs.utils.Errors.error(\"数据库操作出错：\", e);\n");
		sb.append("  }finally{\n");
		sb.append("  rs.close();\n");
		sb.append("  ps.close();\n");
		sb.append("  con.close();");
		sb.append("  }\n");
		if (isList) {
			sb.append("return list;\n");
		} else {
			sb.append("return obj;\n");
		}
	}

	public void writeExecute(StringBuilder sb, StringBuilder ppsb, String sql) {
		sb.append("int i = -1;\n");
		sb.append("try{\n con = java.sql.DriverManager.getConnection(url, user, passWord);\n");
		sb.append("  	String sql = \"" + sql + "\";\n");
		sb.append("  	LOG.info(sql);\n");
		sb.append("  	ps = con.prepareStatement(sql);\n");
		sb.append(ppsb.toString());
		sb.append("    i = ps.executeUpdate();\n");
		sb.append("  } catch (Exception e) {\n");
		sb.append("    org.waps.jobs.utils.Errors.error(\"数据库操作出错：\", e);\n");
		sb.append("  }finally{\n");
		sb.append("  ps.close();\n");
		sb.append("  con.close();");
		sb.append("  }\n");
		sb.append("return i;\n");
	}

	public StringBuilder writePrep(CtClass[] paramtertypes)
			throws NotFoundException, CannotCompileException {
		StringBuilder ptSb = new StringBuilder();
		int n = paramtertypes.length;
		int index = 0;
		for (int i = 0; i < n; i++) {
			CtClass pt = paramtertypes[i];
			String ptName = pt.getName();
			ptName = ptName.substring(ptName.lastIndexOf(".") + 1,
					ptName.length());
			ptName = ptName.substring(0, 1).toUpperCase() + ptName.substring(1);
			if (pt.isPrimitive() || ptName.equals("String")) {
				index = i + 1;
				ptSb.append("	ps.set" + ptName + "(" + index + ",$" + index
						+ ");\n");
			} else {
				IllegalArgumentException ie = new IllegalArgumentException(
						"只允许数据库数据类型");
				Errors.error("接口方法参数不合法:", ie);
				throw ie;
			}
		}
		return ptSb;
	}

	private CtClass createImpl(ClassPool pool, CtClass dao, String interfaceName)
			throws CannotCompileException, NotFoundException {
		String strName = String.class.getName();
		CtClass daoImpl = pool.makeClass(interfaceName + "Impl");
		CtClass its[] = { dao };
		daoImpl.setInterfaces(its);
		CtField userField = new CtField(pool.get(strName), "user", daoImpl);
		userField.setModifiers(Modifier.PRIVATE);
		daoImpl.addField(userField, CtField.Initializer.constant(user));
		CtField pwField = new CtField(pool.get(strName), "passWord", daoImpl);
		pwField.setModifiers(Modifier.PRIVATE);
		daoImpl.addField(pwField, CtField.Initializer.constant(passWord));
		CtField urlField = new CtField(pool.get(strName), "url", daoImpl);
		urlField.setModifiers(Modifier.PRIVATE);
		daoImpl.addField(urlField, CtField.Initializer.constant(url));
		return daoImpl;
	}
}
