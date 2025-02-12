/*
 * Author: Chanchai Lee
 * */
package bigdata.lol;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MapWard {

	public static void main(String[] args) throws Exception {

		Configuration c = new Configuration();
		String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
		/* path for input file */
		Path input = new Path(files[0]);
		/* output directory for job1 */
		Path output = new Path(files[1]);

		Job job1 = new Job(c, "Map WardFrame");
		/* Specific main class in side jar file */
		job1.setJarByClass(MapWard.class);
		/* Specific name for mapper class */
		job1.setMapperClass(WMapper.class);

		/* Modify number of reducers */
		int numReducers = 0;
		job1.setNumReduceTasks(numReducers);

		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job1, input);
		FileOutputFormat.setOutputPath(job1, output);

		System.exit(job1.waitForCompletion(true) ? 0 : 1);

	}

	/*
	 * Note:
	 * 
	 * If MMR < 2000 , low If MMR >= 2000, high
	 * 
	 * Early game: Sub 15 minutes. (<= 900 second) ,Mid game: Between 15-35
	 * minutes. (>900 && <= 2100 second), Late game: 35+ minutes. (>2100 second)
	 */

	/*
	 * Desire output from mapper 
	 * keys: game_id, mmr , won or lost (compare CasterUnitID with localID of winner) 
	 * 
	 * values: x,y,startTime
	 * (early,mid,late)
	 * 
	 */

	public static class WMapper extends Mapper<LongWritable, Text, Text, Text> {

		Text k2 = new Text();
		Text v2 = new Text();
		JSONObject o = new JSONObject();
		int game_id = -1;
		int mmr = -1;
		int winner = -1;
		int teamID = -1;
		JSONObject e = new JSONObject();
		double startTime = -1;
		String interval = null;
		int casterUnitID = -1;
		String result = null;
		String match_result = null;
		int x = -1;
		int y = -1;

		public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException {
			/* Read input */
			String input = value.toString();
			/* Create JSON obejct from input */
			JSONObject obj = new JSONObject(input);

			/* Store game_id */
			game_id = obj.getInt("game_id");
			/* Store mmr */
			mmr = obj.getInt("mmr");
			/* Store winner team */
			winner = obj.getInt("winner");
			/* Create object to store teamID and localID of all players */
			JSONArray players = obj.getJSONArray("players");

			HashMap<Integer, String> playerlists = new HashMap<Integer, String>();
			for (int i = 0; i < players.length(); i++) {

				result = null;
				o = players.getJSONObject(i);

				if (o.getInt("teamID") == winner) {
					result = "won";

				} else {
					result = "lost";

				}

				playerlists.put(o.getInt("localID"), result);
			}

			JSONObject nest = obj.getJSONObject("data");
			JSONArray wardFrames = nest.getJSONArray("wardFrames");

			for (int i = 0; i < wardFrames.length(); i++) {
				startTime = -1;
				casterUnitID = -1;
				match_result = null;
				x = -1;
				y = -1;
				interval = null;

				e = wardFrames.getJSONObject(i);
				startTime = e.getDouble("startTime");
				casterUnitID = e.getInt("casterUnitID");

				for (Map.Entry<Integer, String> m : playerlists.entrySet()) {

					/* if localID == casterUnitID */
					if (m.getKey() == casterUnitID) {
						match_result = m.getValue();
					}

				}

				x = e.getInt("x");
				y = e.getInt("y");

				if (startTime != -1 && casterUnitID != -1 && match_result != null) {
					k2.set(game_id + "," + mmr + "," + match_result + ",");

					if (startTime <= 900) {
						interval = "early";
					} else if (startTime > 900 && startTime <= 2100) {
						interval = "mid";
					} else if (startTime > 2100) {
						interval = "late";
					} else {
						interval = "error";
					}
					v2.set(x + "," + y + "," + interval);

					con.write(k2, v2);
				}

			}
		}

	}
}
