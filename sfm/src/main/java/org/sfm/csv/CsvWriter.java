package org.sfm.csv;

import org.sfm.csv.impl.writer.CsvCellWriter;
import org.sfm.csv.mapper.FieldMapperToAppendableFactory;
import org.sfm.map.Mapper;
import org.sfm.map.MappingContext;
import org.sfm.map.column.ColumnProperty;
import org.sfm.map.column.FormatProperty;
import org.sfm.map.column.FieldMapperColumnDefinition;
import org.sfm.map.MapperConfig;
import org.sfm.map.mapper.ContextualMapper;
import org.sfm.reflect.ReflectionService;
import org.sfm.reflect.TypeReference;
import org.sfm.reflect.meta.ClassMeta;
import org.sfm.tuples.Tuple2;
import org.sfm.utils.ErrorHelper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.Format;
import java.util.Arrays;

/**
 * A CsvWriter allows the caller to write object of type T to an appendable in a specified format. See {@link org.sfm.csv.CsvWriter#from(Class)} to create one.
 * <p>
 * The DSL allows to create a CsvWriter easily. The CsvWriter will by default append the headers on the call to {@link org.sfm.csv.CsvWriter.CsvWriterDSL#to(Appendable)}
 * Because the DSL create a mapper it is better to cache the {@link org.sfm.csv.CsvWriter.CsvWriterDSL}.
 * <br>
 * <code>
 *     CsvWriter csvWriter = CsvWriter.from(MyObject.class).to(myWriter);<br>
 *     csvWriter.append(obj1).append(obj2);<br>
 * </code>
 * <br>
 * You can deactivate that by calling {@link org.sfm.csv.CsvWriter.CsvWriterDSL#skipHeaders()}
 * <br>
 * <code>
 *     CsvWriter csvWriter = CsvWriter.from(MyObject.class).skipHeaders().to(myWriter);<br>
 * </code>
 * <br>
 * You can also specified the column names.
 * <br>
 * <code>
 *     CsvWriter csvWriter = CsvWriter.from(MyObject.class).columns("id", "name").to(myWriter);<br>
 * </code>
 * <br>
 * Or add a column with a specified format
 * <br>
 * <code>
 *     CsvWriter csvWriter = CsvWriter.from(MyObject.class).columns("date", new SimpleDateFormat("yyyyMMdd")).to(myWriter);<br>
 * </code>
 *
 * @param <T> the type of object to write
 */
public class CsvWriter<T>  {

    private final Mapper<T, Appendable> mapper;
    private final Appendable appendable;
    private final MappingContext<T> mappingContext;

    private CsvWriter(Mapper<T, Appendable> mapper, Appendable appendable, MappingContext<T> mappingContext) {
        this.mapper = mapper;
        this.appendable = appendable;
        this.mappingContext = mappingContext;
    }

    /**
     * write the specified value to the underlying appendable.
     * @param value the value to write
     * @return the current writer
     * @throws IOException If an I/O error occurs
     */
    public CsvWriter<T> append(T value) throws IOException {
        try {
            mapper.mapTo(value, appendable, mappingContext);
        } catch(Exception e) {
            ErrorHelper.rethrow(e);
        }
        return this;
    }

    /**
     * Create a DSL on the specified type.
     * @param type the type of object to write
     * @param <T> the type
     * @return a DSL on the specified type
     */
    public static <T> CsvWriterDSL<T> from(Class<T> type) {
        return from((Type)type);
    }

    /**
     * Create a DSL on the specified type.
     * @param typeReference the type of object to write
     * @param <T> the type
     * @return a DSL on the specified type
     */
    public static <T> CsvWriterDSL<T> from(TypeReference<T> typeReference) {
        return from(typeReference.getType());
    }

    /**
     * Create a DSL on the specified type.
     * @param type the type of object to write
     * @param <T> the type
     * @return a DSL on the specified type
     */
    public static <T> CsvWriterDSL<T> from(Type type) {

        ClassMeta<T> classMeta = ReflectionService.newInstance().getClassMeta(type);

        CellWriter cellWriter = CsvCellWriter.DEFAULT_WRITER;

        CsvWriterBuilder<T> builder = CsvWriterBuilder
                .newBuilder(classMeta, cellWriter);

        MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig = MapperConfig.<CsvColumnKey>fieldMapperConfig();
        try {
            builder.defaultHeaders();
            ContextualMapper<T, Appendable> mapper = (ContextualMapper<T, Appendable>) builder.mapper();
            return new DefaultCsvWriterDSL<T>(
                    CsvWriter.<T>toColumnDefinitions(classMeta.generateHeaders()),
                    cellWriter,
                    mapper,
                    classMeta,
                    mapperConfig, false);
        } catch (UnsupportedOperationException e) {
            return new NoColumnCsvWriterDSL<T>(
                    cellWriter,
                    classMeta,
                    mapperConfig, false);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] toColumnDefinitions(String[] header) {
        Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columnDefinitions = new Tuple2[header.length];
        int offset = 0;
        return toColumnDefinitions(header, columnDefinitions, offset);
    }

    private static Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] toColumnDefinitions(String[] header, Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columnDefinitions, int offset) {
        FieldMapperColumnDefinition<CsvColumnKey> identity = FieldMapperColumnDefinition.<CsvColumnKey>identity();
        for(int i = 0; i < header.length; i++) {
            columnDefinitions[i + offset] = new Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>(header[i], identity);
        }
        return columnDefinitions;
    }

    /**
     * the csv writer DSL
     * @param <T> the type of object to write
     */
    public static class CsvWriterDSL<T> {

        protected final Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns;
        protected final ContextualMapper<T, Appendable> mapper;
        protected final CellWriter cellWriter;
        protected final ClassMeta<T> classMeta;
        protected final MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig;
        protected final boolean skipHeaders;

        private CsvWriterDSL(
                Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns,
                CellWriter cellWriter,
                ContextualMapper<T, Appendable> mapper,
                ClassMeta<T> classMeta,
                MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig,
                boolean skipHeaders) {
            this.columns = columns;
            this.mapper = mapper;
            this.cellWriter = cellWriter;
            this.classMeta = classMeta;
            this.mapperConfig = mapperConfig;
            this.skipHeaders = skipHeaders;
        }

        /**
         * Create a writer on the specified appendable for the type T
         * @param appendable the appendable to write to
         * @return a CsvWriter on the specified appendable
         * @throws IOException If an I/O error occurs
         */
        public CsvWriter<T> to(Appendable appendable) throws IOException {
            if (!skipHeaders) {
                addHeaders(appendable);
            }
            return new CsvWriter<T>(mapper, appendable, mapper.newMappingContext());
        }

        private void addHeaders(Appendable appendable) throws IOException {
            for(int i = 0; i < columns.length; i++) {
                if (i != 0) {
                    cellWriter.nextCell(appendable);
                }
                cellWriter.writeValue(columns[i].first(), appendable);

            }
            cellWriter.endOfRow(appendable);
        }

        /**
         * Create a new DSL object identical to the current one but and append the specified columns
         * @param columnNames the list of column names
         * @return the new DSL
         */
        @SuppressWarnings("unchecked")
        public CsvWriterDSL<T> columns(String... columnNames) {
            Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] newColumns =
                    Arrays.copyOf(columns, columns.length + columnNames.length);
            toColumnDefinitions(columnNames, newColumns, columns.length);
            return newColumnMapDSL(classMeta, newColumns, mapperConfig, cellWriter, skipHeaders);
        }

        /**
         * Create a new DSL object identical to the current one but with the specified column added.
         * @param column the column name
         * @param property the column properties
         * @return the new DSL
         */
        @SuppressWarnings("unchecked")
        public CsvWriterDSL<T> column(String column, ColumnProperty... property) {
            Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] newColumns =
                    Arrays.copyOf(columns, columns.length + 1);

            FieldMapperColumnDefinition<CsvColumnKey> columnDefinition =  FieldMapperColumnDefinition.<CsvColumnKey>identity().add(property);
            newColumns[columns.length] = new Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>(column, columnDefinition);

            return newColumnMapDSL(classMeta, newColumns, mapperConfig, cellWriter, skipHeaders);
        }

        /**
         * Create a new DSL object identical to the current one but with the specified column added.
         * @param column the column name
         * @param format the column formatter
         * @return the new DSL
         */
        public CsvWriterDSL<T> column(String column, Format format) {
            return column(column, new FormatProperty(format));
        }

        /**
         * Create a new DSL object identical to the current one but with the specified classMeta.
         * @param classMeta the classMeta
         * @return the new DSL
         */
        public CsvWriterDSL<T> classMeta(ClassMeta<T> classMeta) {
            return newMapDSL(classMeta, columns, mapperConfig, cellWriter, skipHeaders);
        }

        /**
         * Create a new DSL object identical to the current one but with the specified mapperConfig.
         * @param mapperConfig the mapperConfig
         * @return the new DSL
         */
        public CsvWriterDSL<T> mapperConfig(MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig) {
            return newMapDSL(classMeta, columns, mapperConfig, cellWriter, skipHeaders);
        }

        /**
         * Create a new DSL object identical to the current one but with the specified cellWriter.
         * @param cellWriter the cellWriter
         * @return the new DSL
         */
        public CsvWriterDSL<T> cellWriter(CellWriter cellWriter) {
            return newMapDSL(classMeta, columns, mapperConfig, cellWriter, skipHeaders);
        }

        public CsvWriterDSL<T> separator(char separator) {
            if (cellWriter instanceof CsvCellWriter) {
                return newMapDSL(classMeta, columns, mapperConfig, ((CsvCellWriter)cellWriter).separator(separator), skipHeaders);
            }
            throw new IllegalStateException("Custom cell writer set, cannot use schema to alter it");
        }

        public CsvWriterDSL<T> quote(char quote) {
            if (cellWriter instanceof CsvCellWriter) {
                return newMapDSL(classMeta, columns, mapperConfig, ((CsvCellWriter)cellWriter).quote(quote), skipHeaders);
            }
            throw new IllegalStateException("Custom cell writer set, cannot use schema to alter it");
        }

        public CsvWriterDSL<T> endOfLine(String endOfLine) {
            if (cellWriter instanceof CsvCellWriter) {
                return newMapDSL(classMeta, columns, mapperConfig, ((CsvCellWriter)cellWriter).endOfLine(endOfLine), skipHeaders);
            }
            throw new IllegalStateException("Custom cell writer set, cannot use schema to alter it");
        }

        public CsvWriterDSL<T> alwaysEscape() {
            if (cellWriter instanceof CsvCellWriter) {
                return newMapDSL(classMeta, columns, mapperConfig, ((CsvCellWriter)cellWriter).alwaysEscape(), skipHeaders);
            }
            throw new IllegalStateException("Custom cell writer set, cannot use schema to alter it");
        }

        /**
         * Create a new DSL object identical to the current one except it will not append the headers to the appendable.
         * @return the new DSL
         */
        public CsvWriterDSL<T> skipHeaders() {
            return newMapDSL(classMeta, columns, mapperConfig, cellWriter, true);
        }


        public MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig() {
            return mapperConfig;
        }

        protected CsvWriterDSL<T> newColumnMapDSL(
                ClassMeta<T> classMeta,
                Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns,
                MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig,
                CellWriter cellWriter,
                boolean skipHeaders) {

            CsvWriterBuilder<T> builder = new CsvWriterBuilder<T>(classMeta, mapperConfig, new FieldMapperToAppendableFactory(cellWriter), cellWriter);

            for( Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>> col : columns) {
                builder.addColumn(col.first(), col.second());
            }

            ContextualMapper<T, Appendable> mapper = (ContextualMapper<T, Appendable>) builder.mapper();

            return new CsvWriterDSL<T>(columns, cellWriter, mapper, classMeta, mapperConfig, skipHeaders);
        }

        protected CsvWriterDSL<T> newMapDSL(
                ClassMeta<T> classMeta,
                Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns,
                MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig,
                CellWriter cellWriter,
                boolean skipHeaders) {

            CsvWriterBuilder<T> builder = new CsvWriterBuilder<T>(classMeta, mapperConfig, new FieldMapperToAppendableFactory(cellWriter), cellWriter);

            for( Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>> col : columns) {
                builder.addColumn(col.first(), col.second());
            }

            ContextualMapper<T, Appendable> mapper = (ContextualMapper<T, Appendable>) builder.mapper();

            return newCsvWriterDSL(columns, cellWriter, mapper, classMeta, mapperConfig, skipHeaders);
        }

        protected CsvWriterDSL<T> newCsvWriterDSL(Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns,
                                                CellWriter cellWriter,
                                                ContextualMapper<T, Appendable> mapper, ClassMeta<T> classMeta,
                                                MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig,
                                                boolean skipHeaders) {
            return new CsvWriterDSL<T>(columns, cellWriter, mapper, classMeta, mapperConfig, skipHeaders);
        }
    }

    public static class NoColumnCsvWriterDSL<T> extends CsvWriterDSL<T> {
        @SuppressWarnings("unchecked")
        public NoColumnCsvWriterDSL(
                CellWriter cellWriter,
                ClassMeta<T> classMeta,
                MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig, boolean skipHeaders) {
            super(new Tuple2[0], cellWriter, null, classMeta, mapperConfig, skipHeaders);
        }

        @Override
        public CsvWriter<T> to(Appendable appendable) throws IOException {
            throw new IllegalStateException("No columned defined");
        }
        protected NoColumnCsvWriterDSL<T> newCsvWriterDSL(Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns,
                                                  CellWriter cellWriter,
                                                  ContextualMapper<T, Appendable> mapper, ClassMeta<T> classMeta,
                                                  MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig,
                                                  boolean skipHeaders) {
            return new NoColumnCsvWriterDSL<T>(cellWriter, classMeta, mapperConfig, skipHeaders);
        }
    }

    public static class DefaultCsvWriterDSL<T> extends CsvWriterDSL<T> {

        private DefaultCsvWriterDSL(
                Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns,
                CellWriter cellWriter,
                ContextualMapper<T, Appendable> mapper,
                ClassMeta<T> classMeta,
                MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig, boolean skipHeaders) {
            super(columns, cellWriter, mapper, classMeta, mapperConfig, skipHeaders);
        }


        /**
         * Create a new DSL object identical to the current one but with the specified columns instead of the default ones.
         * @param columnNames the list of column names
         * @return the new DSL
         */
        public CsvWriterDSL<T> columns(String... columnNames) {
            return newColumnMapDSL(classMeta, CsvWriter.<T>toColumnDefinitions(columnNames), mapperConfig, cellWriter, skipHeaders);
        }


        /**
         * Create a new DSL object identical to the current one but with the specified column instead of the default ones.
         * @param column the column name
         * @param property the column properties
         * @return the new DSL
         */
        @SuppressWarnings("unchecked")
        public CsvWriterDSL<T> column(String column, ColumnProperty... property) {
            Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] newColumns = new Tuple2[1];

            FieldMapperColumnDefinition<CsvColumnKey> columnDefinition =  FieldMapperColumnDefinition.<CsvColumnKey>identity().add(property);
            newColumns[0] = new Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>(column, columnDefinition);

            return newColumnMapDSL(classMeta, newColumns, mapperConfig, cellWriter, skipHeaders);
        }
        protected CsvWriterDSL<T> newCsvWriterDSL(Tuple2<String, FieldMapperColumnDefinition<CsvColumnKey>>[] columns,
                                                  CellWriter cellWriter,
                                                  ContextualMapper<T, Appendable> mapper, ClassMeta<T> classMeta,
                                                  MapperConfig<CsvColumnKey, FieldMapperColumnDefinition<CsvColumnKey>> mapperConfig,
                                                  boolean skipHeaders) {
            return new DefaultCsvWriterDSL<T>(columns, cellWriter, mapper, classMeta, mapperConfig, skipHeaders);
        }
    }



}
