const std = @import("std");

const HRESULT = i32;
const RO_INIT_MULTITHREADED = @as(u32, 1);

extern "api-ms-win-core-winrt-l1-1-0" fn RoInitialize(
    init_type: u32,
) callconv(.winapi) HRESULT;

extern "api-ms-win-core-winrt-l1-1-0" fn RoUninitialize() callconv(.winapi) void;

pub export fn winrt_initialize_subsystem() i32 {
    const result = RoInitialize(RO_INIT_MULTITHREADED);

    if (result >= 0) {
        std.debug.print("WinRT Thread Subsystem Successfully Engaged. [HRESULT: 0x{X:0>8}]\n", .{@as(u32, @bitCast(result))});
    } else {
        std.debug.print("WinRT Activation Critical Fault. [HRESULT: 0x{X:0>8}]\n", .{@as(u32, @bitCast(result))});
    }

    return result;
}

pub export fn winrt_shutdown_subsystem() void {
    RoUninitialize();
    std.debug.print("WinRT Thread Subsystem Disengaged.\n", .{});
}

pub export fn test_native_connection() void {
    std.debug.print("Hello from Zig! The FFM Panama pipeline is officially working.\n", .{});
}
