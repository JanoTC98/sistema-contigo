# 🧩 Sistema CONTIGO — Gestión Integral de Beneficiarios

Proyecto académico desarrollado en **Java + MySQL** que simula un sistema completo de gestión de beneficiarios inspirado en el programa social peruano **CONTIGO**.  
Incluye registro, actualización, elegibilidad, certificados médicos, padrones de pago, reportes, auditoría, vistas, triggers y procedimientos almacenados.

> ⚠️ *Este proyecto es únicamente educativo y no representa sistemas oficiales del MIDIS.*

---


---

## 🗄️ Base de Datos (MySQL)

El script completo de la BD se encuentra en:
/db/program_contigo.sql
Este archivo contiene:

- Tablas:  
  `beneficiarios`, `autorizados`, `certificados`, `padrones`, `pagos_realizados`, `auditoria_beneficiarios`
- Índices para rendimiento
- Vistas:  
  `v_beneficiarios_elegibles`, `v_resumen_pagos`
- Procedimientos almacenados:  
  `sp_verificar_elegibilidad`, `sp_procesar_pago`
- Triggers de auditoría
- Inserción automática de padrones 2025 (I–VI)

---

## 🚀 Funcionalidades

### ✔ Gestión de Beneficiarios
- Registro
- Actualización (clasificación, ingresos, región)
- Eliminación por fallecimiento
- Verificación de elegibilidad
- Cronograma anual de pagos
- Historial de padrones cobrados

### ✔ Gestión de Autorizados
- Registro
- Validación automática de parentesco permitido

### ✔ Certificados Médicos
- Registro
- Asignación a beneficiarios
- Verificación de vigencia y grado de discapacidad

### ✔ Procesamiento de Pagos
- Determinación de próximo padrón disponible
- Validación con `v_beneficiarios_elegibles`
- Procesamiento mediante SP `sp_procesar_pago`

### ✔ Reportes
- Beneficiarios (general)
- Autorizados
- No elegibles
- Por región
- Fallecidos
- Estadísticas (región, clasificación, elegibilidad)

### ✔ Alertas del Sistema
Detecta beneficiarios:
- Sin certificado
- Con ingresos
- Con clasificación incompatible

---

## 🔧 Requisitos

- Java 11+
- MySQL 8.x
- MySQL Connector/J (driver JDBC)

---

## ▶️ Cómo Ejecutar

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/ProgramaContigo.git
cd ProgramaContigo

