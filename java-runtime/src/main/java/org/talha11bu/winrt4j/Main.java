package org.talha11bu.winrt4j;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        System.out.println("Java application started. Attempting native downcall...");

        Path dllPath = Paths.get("../native-bridge/zig-out/bin/native-bridge.dll").toAbsolutePath();
        
        try (Arena arena = Arena.ofConfined()) {
            
            SymbolLookup nativeLib = SymbolLookup.libraryLookup(dllPath, arena);
            
            Linker linker = Linker.nativeLinker();

            MemorySegment initSym = nativeLib.find("winrt_initialize_subsystem").orElseThrow();
            MemorySegment shutdownSym = nativeLib.find("winrt_shutdown_subsystem").orElseThrow();

            MethodHandle winrtInit = linker.downcallHandle(initSym, FunctionDescriptor.of(ValueLayout.JAVA_INT));
            MethodHandle winrtShutdown = linker.downcallHandle(shutdownSym, FunctionDescriptor.ofVoid());

            System.out.println("--- Booting Subsystem ---");
            int hresult = (int) winrtInit.invokeExact();
            
            if (hresult >= 0) {
                System.out.println("Java side received: Success status confirmation.");
                
                System.out.println("--- Closing Subsystem ---");
                winrtShutdown.invokeExact();
            }
            else{
                System.err.println("Aborting execution loop: Subsystem failed to lock threads.");
            }
        } catch (Throwable e) {
            System.err.println("FFM Downcall Execution Failed!");
            e.printStackTrace();
        }
    }
}