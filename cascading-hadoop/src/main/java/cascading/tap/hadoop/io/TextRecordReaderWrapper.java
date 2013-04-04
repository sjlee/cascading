package cascading.tap.hadoop.io;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.lib.CombineFileSplit;

class TextRecordReaderWrapper extends RecordReaderWrapper<LongWritable,Text>
  {
  // this constructor signature is required by CombineFileRecordReader
  public TextRecordReaderWrapper( CombineFileSplit split, Configuration conf, Reporter reporter, Integer idx ) throws IOException
    {
    super( new TextInputFormat(), split, conf, reporter, idx );
    }
  }
