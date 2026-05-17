using System;
using System.IO;
using System.Collections.Generic;
using System.Text.Json;
using System.Reflection.Metadata;
using System.Reflection.PortableExecutable;

namespace MetadataParser {
    // 1. Define our Intermediate Representation (IR) structures for JSON
    public class WinRTType {
        public string Namespace { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string Kind { get; set; } = string.Empty; // Interface, Class, Enum, etc.
        public List<string> Methods { get; set; } = new List<string>();
    }

    class Program {
        static void Main(string[] args) {
            string winmdPath = @"C:\Windows\System32\WinMetadata\Windows.Foundation.winmd";
            string outputPath = @"../schema/metadata.json"; 

            if (!File.Exists(winmdPath)) {
                Console.WriteLine($"Error: Missing {winmdPath}");
                return;
            }

            using FileStream fs = new FileStream(winmdPath, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
            using PEReader peReader = new PEReader(fs);
            MetadataReader mdReader = peReader.GetMetadataReader();

            var extractedTypes = new List<WinRTType>();
            int limit = 0;

            foreach (TypeDefinitionHandle typeHandle in mdReader.TypeDefinitions) {
                TypeDefinition typeDef = mdReader.GetTypeDefinition(typeHandle);
                string typeName = mdReader.GetString(typeDef.Name);
                string typeNamespace = mdReader.GetString(typeDef.Namespace);

                if (string.IsNullOrEmpty(typeName) || typeName.StartsWith("<")) continue;

                string kind = "Class";
                if ((typeDef.Attributes & System.Reflection.TypeAttributes.Interface) != 0) {
                    kind = "Interface";
                } else if (typeDef.BaseType.IsNil == false) {
                }

                var typeInfo = new WinRTType {
                    Namespace = typeNamespace,
                    Name = typeName,
                    Kind = kind
                };

                foreach (MethodDefinitionHandle methodHandle in typeDef.GetMethods()) {
                    MethodDefinition methodDef = mdReader.GetMethodDefinition(methodHandle);
                    string methodName = mdReader.GetString(methodDef.Name);
                    
                    if (!methodName.StartsWith("get_") && !methodName.StartsWith("put_") && !methodName.StartsWith(".ctor")) {
                        typeInfo.Methods.Add(methodName);
                    }
                }

                extractedTypes.Add(typeInfo);
                
                if (++limit >= 15) break;
            }

            var options = new JsonSerializerOptions { WriteIndented = true };
            string jsonString = JsonSerializer.Serialize(extractedTypes, options);
            
            File.WriteAllText(outputPath, jsonString);
            Console.WriteLine($"Saved processed metadata schema to: {Path.GetFullPath(outputPath)}");
        }
    }
}