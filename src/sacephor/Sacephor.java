package sacephor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Sacephor
{
	private static final String WIN7_ROOT = "HKEY_CLASSES_ROOT\\http\\shell\\open\\command";
	private static final String WIN10_ROOT = "HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\Shell\\Associations\\UrlAssociations\\https\\UserChoice";
	private static final String PROGID= "Progid";

	private static final String APPS ="HKEY_LOCAL_MACHINE\\SOFTWARE\\RegisteredApplications";
	private static final String LOCAL_MACHINE = "HKEY_LOCAL_MACHINE";
	
	private static final String chrome = "HKEY_LOCAL_MACHINE\\Software\\Clients\\StartMenuInternet\\Google Chrome\\Capabilities";
	public static void main(String[] args) throws Exception
	{
		
		String regQuery = "cmd.exe /c reg query ";  
		String key = "\""+WIN10_ROOT+"\"";
				
		Process p = Runtime.getRuntime().exec(regQuery+key);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		String browser = null;
		while((line = reader.readLine())!=null){
			System.out.println(line);
			if(line.contains(PROGID)){
				browser = line.trim().substring(line.trim().lastIndexOf(' ')+1);
				break;
		//		command = line.substring(line.indexOf('"')+1, line.indexOf(".exe")+4);
		//		break;
			}
		}
		reader.close();
		
		p.destroy();

		key = "\""+chrome+"\"";
		String ccc = null;
		if(browser!=null){
			p = Runtime.getRuntime().exec(regQuery+key);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((line = reader.readLine())!=null){
				System.out.println(line);
				if(line.contains("chrome.exe")){
					ccc = line.substring(line.lastIndexOf(":\\")-1, line.indexOf(".exe")+4);
	//				browser = line.trim().substring(line.trim().lastIndexOf(' ')+1);
					break;
			//		command = line.substring(line.indexOf('"')+1, line.indexOf(".exe")+4);
			//		break;
				}
			}
			
			p.destroy();
		}
		Runtime.getRuntime().exec(ccc +" www.baidu.com");
		
	}
	
}