package org.apache.linkis.storage;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.column.Encoding;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.MessageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.parquet.column.Encoding.*;
import static org.apache.parquet.column.Encoding.DELTA_BYTE_ARRAY;
import static org.apache.parquet.column.ParquetProperties.WriterVersion.PARQUET_1_0;
import static org.apache.parquet.column.ParquetProperties.WriterVersion.PARQUET_2_0;
import static org.apache.parquet.format.converter.ParquetMetadataConverter.NO_FILTER;
import static org.apache.parquet.hadoop.ParquetFileReader.readFooter;
import static org.apache.parquet.hadoop.metadata.CompressionCodecName.UNCOMPRESSED;
import static org.apache.parquet.schema.MessageTypeParser.parseMessageType;

public class TestParquet2 {

//
//    @Test
//    public void test() throws Exception {
//        Configuration conf = new Configuration();
//        Path root = new Path("target/tests/TestParquetWriter/");
//        enforceEmptyDir(conf, root);
//        MessageType schema = parseMessageType(
//                "message test { "
//                        + "required binary binary_field; "
//                        + "required int32 int32_field; "
//                        + "required int64 int64_field; "
//                        + "required boolean boolean_field; "
//                        + "required float float_field; "
//                        + "required double double_field; "
//                        + "required fixed_len_byte_array(3) flba_field; "
//                        + "required int96 int96_field; "
//                        + "} ");
//        GroupWriteSupport.setSchema(schema, conf);
//        SimpleGroupFactory f = new SimpleGroupFactory(schema);
//        Map<String, Encoding> expected = new HashMap<String, Encoding>();
//        expected.put("10-" + PARQUET_1_0, PLAIN_DICTIONARY);
//        expected.put("1000-" + PARQUET_1_0, PLAIN);
//        expected.put("10-" + PARQUET_2_0, RLE_DICTIONARY);
//        expected.put("1000-" + PARQUET_2_0, DELTA_BYTE_ARRAY);
//        for (int modulo : asList(10, 1000)) {
//            for (ParquetProperties.WriterVersion version : ParquetProperties.WriterVersion.values()) {
//                Path file = new Path(root, version.name() + "_" + modulo);
//                ParquetWriter<Group> writer = new ParquetWriter<Group>(
//                        file,
//                        new GroupWriteSupport(),
//                        UNCOMPRESSED, 1024, 1024, 512, true, false, version, conf);
//                for (int i = 0; i < 1000; i++) {
//                    writer.write(
//                            f.newGroup()
//                                    .append("binary_field", "test" + (i % modulo))
//                                    .append("int32_field", 32)
//                                    .append("int64_field", 64l)
//                                    .append("boolean_field", true)
//                                    .append("float_field", 1.0f)
//                                    .append("double_field", 2.0d)
//                                    .append("flba_field", "foo")
//                                    .append("int96_field", Binary.fromConstantByteArray(new byte[12])));
//                }
//                writer.close();
//                final List<ColumnDescriptor> columns = schema.getColumns();
//                ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), file).withConf(conf).build();
//                for (int i = 0; i < 1000; i++) {
//                    Group group = reader.read();
//                    assertEquals("test" + (i % modulo), group.getBinary("binary_field", 0).toStringUsingUTF8());
//                    assertEquals(32, group.getInteger("int32_field", 0));
//                    assertEquals(64l, group.getLong("int64_field", 0));
//                    assertEquals(true, group.getBoolean("boolean_field", 0));
//                    assertEquals(1.0f, group.getFloat("float_field", 0), 0.001);
//                    assertEquals(2.0d, group.getDouble("double_field", 0), 0.001);
//                    assertEquals("foo", group.getBinary("flba_field", 0).toStringUsingUTF8());
//                    assertEquals(Binary.fromConstantByteArray(new byte[12]),
//                            group.getInt96("int96_field",0));
//                }
//                reader.close();
//                ParquetMetadata footer = readFooter(conf, file, NO_FILTER);
//                for (BlockMetaData blockMetaData : footer.getBlocks()) {
//                    for (ColumnChunkMetaData column : blockMetaData.getColumns()) {
//                        if (column.getPath().toDotString().equals("binary_field")) {
//                            String key = modulo + "-" + version;
//                            Encoding expectedEncoding = expected.get(key);
//                            assertTrue(
//                                    key + ":" + column.getEncodings() + " should contain " + expectedEncoding,
//                                    column.getEncodings().contains(expectedEncoding));
//                        }
//                    }
//                }
//                assertEquals("Object model property should be example",
//                        "example", footer.getFileMetaData().getKeyValueMetaData()
//                                .get(ParquetWriter.OBJECT_MODEL_NAME_PROP));
//            }
//        }
//    }

}
