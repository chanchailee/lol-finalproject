/*author: Chanchai Lee*/
package championkill;

/*This class is used to map only data that we need from riftwalk.jsons
 * The purpose of this class is do only mapper that relates to minionKillEvents
 * 
 * We want to use output from this mapper to identify that
 * 
 * Does number of championKill in each game correspond to winning percentage for winner team?
 * 
 * Output:
 * 		key: "championKill_matters"
 * 		values: "1 if number of ward correspond to team_win-rate
 * 				"0 if otherwise"
 * 				,
 * 				"mmr"
 * 					if mmr< 2000 , "low" mmr
 * 					else if mmr >=2000 , "high" mmr*/
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

public class ChampionKillMapData {

	public static void main(String[] args) throws Exception {

		Configuration c = new Configuration();
		String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
		/* path for input file */
		Path input = new Path(files[0]);
		/* output directory for job1 */
		Path output = new Path(files[1]);

		Job job1 = new Job(c, "ChampionKill Map Data");
		/* Specific main class in side jar file */
		job1.setJarByClass(ChampionKillMapData.class);
		/* Specific name for mapper class */
		job1.setMapperClass(ChampionKillMapper.class);

		/* Modify number of reducers */
		int numReducers = 0;
		job1.setNumReduceTasks(numReducers);

		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job1, input);
		FileOutputFormat.setOutputPath(job1, output);

		System.exit(job1.waitForCompletion(true) ? 0 : 1);

	}

	public static class ChampionKillMapper extends Mapper<LongWritable, Text, Text, Text> {

		Text k2 = new Text();
		Text v2 = new Text();
		JSONObject o = new JSONObject();
		JSONObject e = new JSONObject();
		String out = null;
		int game_id = -1;
		int mmr = -1;
		int winner = -1;
		int killerUnitID = -1;
		int wonTeam_championKillCount = 0;
		int lostTeam_championKillCount = 0;
		int championKill_matters = -1;
		String result = null;
		String match_result = null;
		String mmr_out = null;

		public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException {

			game_id = -1;
			mmr = -1;
			winner = -1;
			killerUnitID = -1;
			result = null;
			mmr_out = null;
			/* Read input */
			String input = value.toString();
			/* Create JSON obejct from input */
			JSONObject obj = new JSONObject(input);

			/* Store mmr */
			mmr = obj.getInt("mmr");
			if (mmr < 2000 && mmr != -1) {
				mmr_out = "low";
			} else if (mmr >= 2000 && mmr != -1) {
				mmr_out = "high";
			} else {
				mmr_out = "error";
			}
			/* Store winner team */
			winner = obj.getInt("winner");
			/* Create object to store teamID and localID of all players */
			JSONArray players = obj.getJSONArray("players");

			/*
			 * Store HashMap object which can identify win-team or lost-team for
			 * each localID key: localID values: won or lost
			 */
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
			JSONArray killFrames = nest.getJSONArray("killFrames");

			wonTeam_championKillCount = 0;
			lostTeam_championKillCount = 0;
			championKill_matters = -1;
			
			/*This loop is used to count total minion-killed for both won and lost team*/
			for (int i = 0; i < killFrames.length(); i++) {

				killerUnitID = -1;
				

				e = killFrames.getJSONObject(i);

				killerUnitID = e.getInt("killerUnitID");

				/*
				 * This loop would return won or lost team for particular
				 * killerUnitID
				 */
				for (Map.Entry<Integer, String> m : playerlists.entrySet()) {
					match_result = null;
					/* if localID == killerUnitID */
					if (m.getKey() == killerUnitID) {
						match_result = m.getValue();
					}

				}
				/*if the casterUnitID in won team, then wonTeam_championKillCount++
				 * otherwise lostTeam_championKillCount++
				 * */
				if (match_result=="won"&&match_result!=null) {
					wonTeam_championKillCount++;
				} else if (match_result=="lost"&&match_result!=null){
					lostTeam_championKillCount++;
				}

			}

			if (wonTeam_championKillCount > lostTeam_championKillCount) {
				championKill_matters = 1;
			} else if (wonTeam_championKillCount <= lostTeam_championKillCount) {
				championKill_matters= 0;
			} else {
				championKill_matters= -1;
			}

			k2.set("championKill_matters,");

			if (championKill_matters != -1&&!mmr_out.equals(null)) {
				
				v2.set(championKill_matters + "," + mmr_out);
			}

			con.write(k2, v2);

		}
	}

}
