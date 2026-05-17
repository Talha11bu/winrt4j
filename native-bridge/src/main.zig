const std = @import("std");

pub export fn test_native_connection() void {
    std.debug.print("Hello from Zig! The FFM Panama pipeline is officially working.\n", .{});
}
