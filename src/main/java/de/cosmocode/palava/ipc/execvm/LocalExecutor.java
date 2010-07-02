/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.ipc.execvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.cosmocode.palava.ipc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements the {@link IpcCommandExecutor} with local vm class lookups.
 *
 * @author Tobias Sarnowski
 * @author Willi Schoenborn
 */
public final class LocalExecutor implements IpcCommandExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(LocalExecutor.class);

    private final ConcurrentMap<String, Class<? extends IpcCommand>> cache = new MapMaker().softValues().makeMap();

    private final Injector injector;

    private final IpcCallFilterChainFactory chainFactory;

    @Inject
    protected LocalExecutor(Injector injector, IpcCallFilterChainFactory chainFactory) {
        this.injector = Preconditions.checkNotNull(injector, "Injector");
        this.chainFactory = Preconditions.checkNotNull(chainFactory, "ChainFactory");
    }

    @Override
    public Map<String, Object> execute(final String name, final IpcCall call) throws IpcCommandExecutionException {
        Preconditions.checkNotNull(call, "Call");

        final Class<? extends IpcCommand> commandClass;

        try {
            commandClass = load(name);
        } catch (ClassNotFoundException e) {
            throw new IpcCommandNotAvailableException(name, e);
        } catch (ClassCastException e) {
            // caller should not know the difference between "class not found" and "class is no command"
            throw new IpcCommandNotAvailableException(name, e);
        }

        final IpcCommand command = injector.getInstance(commandClass);
        final IpcCallFilterChain chain = chainFactory.create(new IpcCallFilterChain() {

            @Override
            public Map<String, Object> filter(IpcCall call, IpcCommand command) throws IpcCommandExecutionException {
                final Map<String, Object> result = Maps.newLinkedHashMap();
                LOG.debug("Executing {}", command);
                try {
                    command.execute(call, result);
                } catch (IpcCommandExecutionException e) {
                    LOG.debug("An expected exception was thrown while executing " + command, e);
                    throw e;
                } catch (RuntimeException e) {
                    LOG.error("An unexpected exception was thrown while executing " + command, e);
                    throw e;
                }
                return result;
            }

        });

        return chain.filter(call, command);
    }

    private Class<? extends IpcCommand> load(String name) throws ClassNotFoundException {
        final Class<? extends IpcCommand> cached = cache.get(name);
        if (cached == null) {
            final Class<?> raw = Class.forName(name);
            final Class<? extends IpcCommand> type = raw.asSubclass(IpcCommand.class);
            LOG.trace("Putting {}/{} into cache", name, type);
            cache.put(name, type);
            return type;
        } else {
            LOG.trace("Returning {} from cache", cached);
            return cached;
        }
    }

}
