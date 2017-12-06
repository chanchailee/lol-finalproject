/*
 * Author: Chanchai Lee
 * */
package lol.kill;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

import bigdata.lol.MapWard;
import bigdata.lol.MapWard.WMapper;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MapKill {

	public static void main(String[] args) throws Exception {

		Configuration c = new Configuration();
		String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
		/* path for input file */
		Path input = new Path(files[0]);
		/* output directory for job1 */
		Path output = new Path(files[1]);

		Job job1 = new Job(c, "Map KillFrame");
		/* Specific main class in side jar file */
		job1.setJarByClass(MapKill.class);
		/* Specific name for mapper class */
//		job1.setMapperClass(MKMapper.class);

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

}
