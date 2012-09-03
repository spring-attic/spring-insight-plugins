/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.hadoop;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.springsource.insight.util.FileUtil;

public class WordCount extends Configured implements Tool {
	public WordCount () {
		super();
	}

   static public class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
      final private static LongWritable ONE = new LongWritable(1);
      private Text tokenValue = new Text();

      @Override
      protected void map(LongWritable offset, Text text, Context context) throws IOException, InterruptedException {
         for (String token : text.toString().split("\\s+")) {
            tokenValue.set(token);
            context.write(tokenValue, ONE);
         }
      }
   }

   static public class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
      private LongWritable total = new LongWritable();

      @Override
      protected void reduce(Text token, Iterable<LongWritable> counts, Context context)
            throws IOException, InterruptedException {
         long n = 0;
         for (LongWritable count : counts)
            n += count.get();
         total.set(n);
         context.write(token, total);
         System.out.println(token+","+total);
      }
   }

   public int run(String[] args) throws Exception {
	   String INPUT="src/test/resources";
	   String OUTPUT="target/out";
	   
	   Configuration conf = new Configuration();
	   File			 targetFolder = FileUtil.detectTargetFolder(getClass());
	   if (targetFolder == null) {
		   throw new IllegalStateException("Cannot detect target folder");
	   }
	   File	tempFolder = new File(targetFolder, "temp");
	   conf.set("hadoop.tmp.dir", tempFolder.getAbsolutePath());

	   Job job = new Job(conf, "wordcount");
      job.setJarByClass(WordCount.class);

      job.setMapperClass(WordCountMapper.class);
      job.setCombinerClass(WordCountReducer.class);
      job.setReducerClass(WordCountReducer.class);

      job.setInputFormatClass(TextInputFormat.class);
      job.setOutputFormatClass(TextOutputFormat.class);

      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(LongWritable.class);
      
      FileUtils.deleteDirectory(new File(OUTPUT)); // delete old output data
      FileInputFormat.addInputPath(job, new Path(INPUT));
      FileOutputFormat.setOutputPath(job, new Path(OUTPUT));

      return job.waitForCompletion(true) ? 0 : -1;
   }

   public static void main(String[] args) throws Exception {
      System.exit(ToolRunner.run(new WordCount(), args));
   }
}
