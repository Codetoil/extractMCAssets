package io.github.littoil.extractMCAssets;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class extractMCAssets {
	//A map from argument type
	private static Map<String, String> argList = new HashMap<>();
	private static String mcDir;
	private static String assetVersion;
	private static String copyTo;

	/**
	 * Main method, every method is called from here.
	 * @param args
	 */
	public static void main(String[] args)
	{
		getArguments(args);
		System.out.println();
		System.out.println("Arguments:");
		argList.forEach((arg, param) -> {
			System.out.println("(" + arg + ", " +param + ")");
		});
		System.out.println();
		if (argList.containsKey("mcDir") && argList.containsKey("assetVersion") && argList.containsKey("copyTo"))
		{
			System.out.println("Have Arguments! Extracting assets!");
			mcDir = deobfMcDir(argList.get("mcDir"));
			assetVersion = argList.get("assetVersion");
			copyTo = deobfMcDir(argList.get("copyTo"));
			if (ExtractAssets())
			{
				System.out.println("Succeeded Extracting Assets!");
				//System.out.println(mcDir);
			} else
			{
				System.err.println("Failed Extracting Assets!");
			}
		}
		else
		{
			dispHelpMessage();
		}
	}

	/**
	 * Turns the given arguments list into a map of Argument types and values, stored into argList
	 * @param args
	 */
	private static void getArguments(String[] args)
	{
		argList.clear();
		//List<Duel<String, String>> outputList = new ArrayList<Duel<String, String>>();
		for (int i = 0; i < args.length - 1; i += 2) // I is argument index
		{
			if (args[i].startsWith("--"))
			{
				argList.put(args[i].substring(2), args[i+1]);
			} else
			{
				System.err.println("[ERROR] Element " + i + " (\""+ args[i] +"\") does not start with \"--\"! Skipping!");
			}
		}
	}

	/**
	 * Display's the program command line help message
	 */
	private static void dispHelpMessage()
	{
		System.out.println("java -jar extractMCAssets.jar --mcDir <Minecraft Directory>[Using *s instead of spaces] --assetVersion <Version of Assets you want to extract> --copyTo <folder to copy to>");
	}

	/**
	 * Main method to extract the assets of minecraft
	 * @return Success(true) vs Fail(false)
	 */
	private static boolean ExtractAssets()
	{
		boolean result = false;
		getAssets();
		result = true;
		return result;
	}

	/**
	 * Retur+n the File of a directory/file in a folder. Directory must end in "/" or "\\"
	 * @param directoryIn
	 * @param nameOfThing
	 * @return
	 */
	private static File getFileOrFolder(String directoryIn, String nameOfThing)
	{
		return Paths.get(directoryIn, nameOfThing).toFile();
	}

	private static boolean getAssets()
	{
		File indexOfAssets = getFileOrFolder(mcDir, "assets/indexes/" + assetVersion + ".json");
		try {
			BufferedReader indexOfAssetsReader = Files.newBufferedReader(indexOfAssets.toPath());
			Stream<String> stringStream = indexOfAssetsReader.lines();
			interpret(stringStream);
			indexOfAssetsReader.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	private static boolean interpret(Stream<String> stringStream)
	{
		Map<String, String> bimapPathHash = new HashMap<String, String>();
		JsonObject objects = getJSONObject(stringStream).get("objects").getAsJsonObject();
		objects.keySet().forEach((key) -> {
			JsonObject elementOfKey = objects.get(key).getAsJsonObject();
			String hash = elementOfKey.get("hash").getAsString();
			bimapPathHash.put(key, hash);
		});
		lookAtFiles(bimapPathHash);
		System.out.println();
		return false;
	}
	private static JsonObject getJSONObject(Stream<String> stringStream)
	{
		String json = "";
		StringBuilder jsonBuilder = new StringBuilder(json);
		// Map of path to hash
		Iterator<String> iterator = stringStream.iterator();
		while (iterator.hasNext())
		{
			jsonBuilder.append(iterator.next());
		}
		json = jsonBuilder.toString();
		//System.out.println(jsonBuilder);
		//System.out.println(json);
		//Return JSON of thing
		return new JsonParser().parse(json).getAsJsonObject();
	}

	private static void delete1(File file) {
		if (file.isFile())
		{
			file.delete();
			return;
		}
		File[] files = file.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()) {
					delete1(f);
				} else {
					f.delete();
				}
			}
		}
		file.delete();
	}

	private static void lookAtFiles(Map<String, String> map)
	{
		String objectsFolder = mcDir + "assets/objects/";
		//File folderToCopyTo = new File(copyTo);s
		delete1(new File(copyTo));
		new File(copyTo).mkdirs();
		map.forEach((key, hash) -> {
			String filePathString = objectsFolder + hash.substring(0,2) + "/" + hash;
			File filePath = new File(filePathString);
			if (filePath.isFile())
			{
				new File(copyTo + key).getParentFile().mkdirs();
				//Copy to new directory
				try {
					Files.copy(filePath.toPath(), Paths.get(copyTo + key));
					System.out.println("Copied " + key + " to " + copyTo);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				//filePath
			}
		});
	}

	private static String deobfMcDir(String dirOld)
	{
		//Get the StringBuilder for this string
		StringBuilder mcDirBuffer = new StringBuilder(dirOld);

		//Turn all * into ' 's so that we can have spaces in the directory
		while (mcDirBuffer.indexOf("*") != -1) //
		{
			mcDirBuffer.setCharAt(mcDirBuffer.indexOf("*"),' ');
		}


		if (mcDirBuffer.length() >= 2)
		{
			//If last two characters are not "\\", or if last single letter is not "/", append a "/" so that the file is considered a directory
			if (!mcDirBuffer.substring(mcDirBuffer.length() - 2).equals("\\") || !mcDirBuffer.substring(mcDirBuffer.length() - 1).equals("/"))
			{
				mcDirBuffer.append("/");
			}
		}
		else
		{
			mcDirBuffer.append("/");
		}
		return mcDirBuffer.toString();
	}
}
