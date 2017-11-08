package application.gameFileParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LuaToJsonConverter
{
	/**
	 * TODO this will be done manually to begin with, however parsing the .lua allows us to remain up to date with changing Factorio
	 * versions.
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static JSONArray getLuaRecipiesAsJson(File luaRecipesFile) throws ParseException, FileNotFoundException, IOException
	{
		// TODO accept an actual .lua file and convert it to a json string in code

		// I've cheated for now and hand crafted a json file from the lua, mostly using regexes and basic copy/replace
		if (luaRecipesFile.getName().endsWith(".json")) {
			JSONParser parser = new JSONParser();
			Object parsedJsonString = parser.parse(new FileReader(luaRecipesFile));

			if (parsedJsonString instanceof JSONArray) {
				return (JSONArray) parsedJsonString;
			}
		}

		throw new ParseException(ParseException.ERROR_UNEXPECTED_TOKEN);
	}
}
