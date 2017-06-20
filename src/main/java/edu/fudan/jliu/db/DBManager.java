package edu.fudan.jliu.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import edu.fudan.jliu.util.DBUtil;

/**
 * @author jkyan
 * database manager
 */
public class DBManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
		
	public static final String CAMERA_INFO_TABLE = "camera_info";
	public static final String CREATE_CAMERA_INFO_TABLE = "CREATE TABLE camera_info("
			+ "stream_id varchar(255) NOT NULL, "
			+ "address varchar(255) NOT NULL, "
			+ "valid tinyint NOT NULL DEFAULT 0, "
			+ "width int NOT NULL DEFAULT 0, "
			+ "height int NOT NULL DEFAULT 0, "
			+ "frame_rate float NOT NULL DEFAULT 0.0, "
			+ "primary key(stream_id));";
	public static final String CAMERA_INFO_TABLE_SIMPLE_COLS = "(stream_id, address)";
	public static final String CAMERA_INFO_TABLE_COLS = "(stream_id, address, valid, width, height, frame_rate)";
	
	public static final String RAW_RTMP_TABLE = "raw_rtmp";
	public static final String CREATE_RAW_RTMP_TABLE = "CREATE TABLE raw_rtmp("
			+ "stream_id varchar(255) NOT NULL, "
			+ "rtmp_addr varchar(255) NOT NULL, "
			+ "host varchar(255) NOT NULL, "
			+ "pid bigint NOT NULL, "
			+ "valid tinyint NOT NULL DEFAULT 0, "
			+ "primary key(stream_id));";
	public static final String RAW_RTMP_TABLE_COLS = "(stream_id, rtmp_addr, host, pid, valid)";
	
	public static final String EFFECT_RTMP_TABLE = "effect_rtmp";
	public static final String CREATE_EFFECT_RTMP_TABLE = "CREATE TABLE effect_rtmp("
			+ "id int AUTO_INCREMENT NOT NULL, "
			+ "stream_id varchar(255) NOT NULL, "
			+ "effect_type varchar(255) NOT NULL, "
			+ "effect_params text, "
			+ "rtmp_addr varchar(255) NOT NULL, "
			+ "topo_name varchar(255) NOT NULL, "
			+ "valid tinyint NOT NULL DEFAULT 0, "
			+ "primary key(id));";
	public static final String EFFECT_RTMP_TABLE_COLS = "(stream_id, effect_type, effect_params, rtmp_addr, topo_name, valid)";

	public static final String TOPOLOGY_TABLE = "topology";
    public static final String TOPOLOGY_TABLE_COLS = "(topo_name, topo_id, worker_num, owner, uptime_secs, task_num, " +
            "executor_num, status)";


    public static final String TOPOLOGY_COMPONENT_INFO_TABLE = "topology_component_info";
    public static final String TOPOLOGY_COMPONENT_INFO_COLS = "(topo_name, component_id, type, executor_num, " +
            "task_num, alltime_processed, alltime_failed, alltime_latency)";


    public static final String TOPOLOGY_WORKER_INFO_TABLE = "topology_worker_info";
    public static final String TOPOLOGY_WORKER_INFO_TABLE_COLS = "(topo_name, host, pid, port, cpu_usage, memory_usage)";


	private Map<String, String> createTableMap = new HashMap<>();

	public static void main(String[] args) {
		DBManager.getInstance();
	}

	private DriverManagerDataSource dataSource = null;
	private static final DBManager dbManager = new DBManager();
	private JdbcTemplate jdbcTemplate = null;

	/**
	 * Get DBManager Instance
	 */
	public static DBManager getInstance() {
		return dbManager;
	}

	/**
	 * Initialize MySql
	 */
	private DBManager() {
		initDataSource();
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);

		dropAllTables();

		try {
			initAllTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * initiate the data source
	 */
	private void initDataSource() {
		dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		String dbUrl = DBUtil.getDBIp();
		String username = DBUtil.getDBUser();
		String password = DBUtil.getDBPassword();
		String dbName = DBUtil.getDBName();
		int port = DBUtil.getDBPort();
		String mysqlUrl = "jdbc:mysql://" + dbUrl + ":" + port + "/" + dbName + "?autoReconnect=true";
		System.out.println(mysqlUrl);
		dataSource.setUrl(mysqlUrl);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
	}

	/**
	 * Create all necessary table
	 * 
	 * @throws SQLException
	 */
	private void initAllTables() throws SQLException {
		createTableMap.put(CAMERA_INFO_TABLE, CREATE_CAMERA_INFO_TABLE);
		createTableMap.put(RAW_RTMP_TABLE, CREATE_RAW_RTMP_TABLE);
		createTableMap.put(EFFECT_RTMP_TABLE, CREATE_EFFECT_RTMP_TABLE);
		for (String table : createTableMap.keySet()) {
			if (!validateTableExist(table)) {
				this.jdbcTemplate.execute(createTableMap.get(table));
			}
		}
		logger.info("cerate all tables...");
	}

	private void dropAllTables() {
		dropTable(CAMERA_INFO_TABLE);
		dropTable(RAW_RTMP_TABLE);
		dropTable(EFFECT_RTMP_TABLE);
		logger.info("drop all Tables...");
	}

	/**
	 * validate if the table exist
	 * 
	 * @param tableName
	 * @return
	 */
	public boolean validateTableExist(String tableName) {
		ResultSet rs;
		boolean flag = false;
		try {
			DatabaseMetaData meta = dataSource.getConnection().getMetaData();
			String type[] = { "TABLE" };
			rs = meta.getTables(null, null, tableName, type);
			flag = rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * Get DriverManagerDatasource Instance
	 */
	public DriverManagerDataSource getDataSource() {
		return dataSource;
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public void dropTable(String tableName) {
		String query = "DROP TABLE " + tableName + ";";
		try {
			this.jdbcTemplate.execute(query);
		} catch (Exception e) {
			logger.warn("drop table {} failed due to {}", tableName, e.getMessage());
		}
	}

	public void clearAllRecords(String tableName) {
		String query = "DELETE FROM " + tableName + ";";
		this.jdbcTemplate.execute(query);
	}
}