/*
 * Author: Chanchai Lee
 * */
package bigdata.lol;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;


/*
 * There purpose of this class is to find all ward positional x,y in all game
 * 
 * This class uses input from MapWard.java's output (MapWard.csv)
 * 
 * input format:  game_id, mmr , match_result, x, y,interval
 * 
 * 
 * output:
 * 	KEY: x,y,
 * 	Value : sum num of wards
 * */

public class PositionalWardCount {
	
	public static void main(String[] args) throws Exception {

		Configuration c = new Configuration();
		String[] files = new GenericOptionsParser(c, args).getRemainingArgs();
		/* path for input file */
		Path input = new Path(files[0]);
		/* output directory for job1 */
		Path output = new Path(files[1]);

		Job job1 = new Job(c, "Find Ward Positional");
		/* Specific main class in side jar file */
		job1.setJarByClass(PositionalWardCount.class);
		/* Specific name for mapper class */
		job1.setMapperClass(PWCMapper.class);
		/* Specific name for reducer class */
		job1.setReducerClass(PWCReducer.class);

		/* Modify number of reducers */
		int numReducers = 1;
		job1.setNumReduceTasks(numReducers);

		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job1, input);
		FileOutputFormat.setOutputPath(job1, output);

		System.exit(job1.waitForCompletion(true) ? 0 : 1);
	}
	
	
	
	
	public static class PWCMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		Text k2 = new Text();
		IntWritable v2 = new IntWritable();
		String x ="";
		String y = "";

		public void map(LongWritable key, Text value, Context con) throws IOException, InterruptedException {
			String data = value.toString();
			String[] lines = data.split("\n");

			for (String line : lines) {

				x ="";
				y = "";
				String[] items = line.split(",");

				/*
				 * items[0] = game_id , items[1] = mmr , items[2] = match_result
				 * items[3] = x ,items[4] = y ,items[5] = interval
				 */
					
				/*round x,y by substring*/
				x = items[3].trim();
				y = items[4].trim();
					k2.set(x.substring(0, x.length()-1) + ","+ y.substring(0, y.length()-1)+",");
					v2.set(1);

					/* write mapper output */
					con.write(k2, v2);

				

			}
		}
	}
	
	public static class PWCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		IntWritable v3 = new IntWritable();
		int ward_sum =0;

		public void reduce(Text k2, Iterable<IntWritable> values, Context con) throws IOException, InterruptedException {

			ward_sum=0;
			for (IntWritable value : values) {
				ward_sum+=value.get();
			}
			
			v3.set(ward_sum);
			con.write(k2, v3);

		}
	}

	

}
