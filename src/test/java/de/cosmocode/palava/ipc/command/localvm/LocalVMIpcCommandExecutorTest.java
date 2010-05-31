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

package de.cosmocode.palava.ipc.command.localvm;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.cosmocode.palava.ipc.AbstractIpcCommandExecutorTest;
import de.cosmocode.palava.ipc.DefaultIpcCallFilterChainFactoryModule;
import de.cosmocode.palava.ipc.IpcCallFilterChainFactory;
import de.cosmocode.palava.ipc.IpcCommandExecutor;

/**
 * Tests {@link LocalVMIpcCommandExecutor}.
 *
 * @author Willi Schoenborn
 */
public final class LocalVMIpcCommandExecutorTest extends AbstractIpcCommandExecutorTest {

    @Override
    protected IpcCommandExecutor unit() {
        final Injector injector = Guice.createInjector(new DefaultIpcCallFilterChainFactoryModule());
        final IpcCallFilterChainFactory factory = injector.getInstance(IpcCallFilterChainFactory.class);
        return new LocalIpcCommandExecutor(injector, factory);
    }

}
