package com.github.msemitkin.financie.lambda;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

//@Configuration
public class SnapStartConfig implements Resource {
    private static final Logger logger = LoggerFactory.getLogger(SnapStartConfig.class);

    private final DataSource dataSource;


    public SnapStartConfig(DataSource dataSource) {
        this.dataSource = dataSource;
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        Connection connection = dataSource.getConnection();
        if (!connection.isClosed()) {
            logger.info("Closing connection");
            connection.close();
        } else {
            logger.info("Connection is already closed");
        }
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) {
        //no-op
    }


}
