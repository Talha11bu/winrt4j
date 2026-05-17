# winrt4j 🚀

A modern, low-overhead Windows Runtime (WinRT) projection for Java. This project bridges the gap between the Java Virtual Machine (JVM) and modern Windows system components or UI frameworks using **Project Panama (Foreign Function & Memory API)** and a highly efficient **Zig flattening layer**.

> ⚠️ **Project Status: Active Development / Experimental PoC**
> This repository is in its early foundational stages. The core end-to-end native downcall pipeline is functional, and we are building out the metadata parsing and code-generation pipelines.

---

## 🏛️ Architectural Overview & Design Philosophy

WinRT (Windows Runtime) is built on an advanced evolution of **COM (Component Object Model)**. In COM, everything is driven by interfaces, virtual function tables (VTables), and strict reference counting (`AddRef` and `Release`).

While Java's modern Foreign Function & Memory (FFM) API is incredibly fast, it is structurally designed to call **flat, C-style functions** rather than complex C++ classes or COM VTables directly. Mapping COM vtable offsets manually inside Java creates fragile pointer-arithmetic boilerplate.

To solve this, `winrt4j` employs a **Flattening Architecture**. We distribute tasks across a decoupled **Three-Tier System** to translate native object-oriented Windows APIs into zero-overhead flat functions that Java can cleanly interact with.

```text
┌────────────────────────────────────────────────────────┐
│                  BUILD-TIME PIPELINE                   │
│                                                        │
│  ┌─────────────────┐       ┌────────────────────────┐  │
│  │ metadata-parser │ ───>  │      metadata.json     │  │
│  │      (C#)       │       │   Intermediate (IR)    │  │
│  └─────────────────┘       └────────────────────────┘  │
└─────────────────────────────────────────┬──────────────┘
                                          │ (Code Generation Engine)
                                          ▼
┌────────────────────────────────────────────────────────┐
│                   RUNTIME EXECUTION                    │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │                  java-runtime                    │  │
│  │  High-Level Idiomatic API (FluentButton, etc.)   │  │
│  │  ──────────────────────────────────────────────  │  │
│  │  Low-Level FFM Panama Layer (MemorySegments)     │  │
│  └───────────────────────┬──────────────────────────┘  │
│                          │ (Flat C ABI Calls)          │
│                          ▼                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │                 native-bridge                    │  │
│  │  Zig DLL: Reference Counting & COM Activator     │  │
│  └───────────────────────┬──────────────────────────┘  │
│                          │ (Native COM/C++ Code)       │
│                          ▼                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │               Windows OS / WinRT                 │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘