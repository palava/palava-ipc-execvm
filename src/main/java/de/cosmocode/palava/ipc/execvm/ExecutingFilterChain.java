package de.cosmocode.palava.ipc.execvm;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import de.cosmocode.palava.ipc.IpcCall;
import de.cosmocode.palava.ipc.IpcCallFilterChain;
import de.cosmocode.palava.ipc.IpcCommand;
import de.cosmocode.palava.ipc.IpcCommandExecutionException;


/**
 * A {@link IpcCallFilterChain} implementation that executes the given command.
 *
 * @author Willi Schoenborn
 */
enum ExecutingFilterChain implements IpcCallFilterChain {
    
    INSTANCE;
    
    private static final Logger LOG = LoggerFactory.getLogger(ExecutingFilterChain.class);
    
    @Override
    public Map<String, Object> filter(IpcCall call, IpcCommand command) throws IpcCommandExecutionException {
        final Map<String, Object> result = Maps.newLinkedHashMap();
        LOG.debug("Executing {}", command);
        
        try {
            command.execute(call, result);
        } catch (IpcCommandExecutionException e) {
            LOG.debug("An expected exception was thrown while executing " + command, e);
            throw e;
            /* CHECKSTYLE:OFF */
        } catch (RuntimeException e) {
            /* CHECKSTYLE:ON */
            LOG.error("An unexpected exception was thrown while executing " + command, e);
            throw e;
        }
        
        return result;
    }
    
}
