






package net.mingsoft.mdiy.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public abstract class BaseTagClassService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    public abstract Object excute(Map map);
}
