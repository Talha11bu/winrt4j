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
            
            MemorySegment functionSymbol = nativeLib.find("test_native_connection")
                .orElseThrow(() -> new RuntimeException("Failed to locate native symbol: test_native_connection"));
            
            FunctionDescriptor descriptor = FunctionDescriptor.ofVoid();
            
            MethodHandle nativeBridgeCall = Linker.nativeLinker().downcallHandle(functionSymbol, descriptor);
            
            System.out.println("--- Entering Native Space ---");
            nativeBridgeCall.invokeExact();
            System.out.println("--- Returned to JVM Space ---");

        } catch (Throwable e) {
            System.err.println("🚨 FFM Downcall Execution Failed!");
            e.printStackTrace();
        }
    }
}