# 🗺️ HTML Image Map Ultra Pro — God Mode 

![Java Version](https://img.shields.io/badge/Java-17+-orange?logo=java)
![Swing](https://img.shields.io/badge/GUI-Swing%20%2F%20AWT-blue)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)
![Status](https://img.shields.io/badge/Mode-God%20Mode-red)

<p align="center">
  <img src="src/Screen_Shot/screenn.png" alt="Modern Picker Preview" width="500">
</p>

**HtmlHelperGodMode** is a high-performance desktop utility designed for visual creation of HTML `<map>` elements. Stop calculating coordinates manually—simply drop an image, draw your shapes, and get production-ready HTML code instantly.

---

## 📺 About The Project

This tool is built for front-end developers and UI designers who need precision when creating interactive image maps. Unlike web-based alternatives, **God Mode** offers a desktop-native experience with smooth zooming, local file handling, and advanced shape manipulation.

### Core Features:
* **⚡ Instant Mapping:** Draw clickable areas directly over your images.
* **🛠️ Geometric Arsenal:** * `RECT` — Classic rectangular zones.
    * `CIRCLE` — Perfect for icons and circular UI elements.
    * `RHOMBUS` — Diamond-shaped polygons for unique design layouts.
* **🔍 Ultra-Zoom Engine:** Up to 10x zoom for pixel-perfect edge detection.
* **📐 Interactive Editor:** Select, move, and resize shapes using 8-point transformation handles.
* **📋 Smart Export:** Automatically generates `<area>` tags and copies them to your clipboard.

---

## ✅ Feature Checklist

- [x] **Drag-and-Drop** — Load images by dropping them directly into the canvas.
- [x] **Infinite Canvas** — Right-click panning to navigate large high-res images.
- [x] **Real-time Feedback** — Sidebar list updates coordinates instantly as you draw.
- [x] **Modern Dark UI** — Aesthetic minimalist theme for reduced eye strain.
- [x] **High Fidelity** — Powered by `Graphics2D` with full Anti-aliasing.

---

## 🎮 Controls & Hotkeys

| Input | Action |
| :--- | :--- |
| **Left Click (Drag)** | Draw a new shape / Move existing shape |
| **Right Click (Drag)** | Pan across the image (Camera move) |
| **Mouse Wheel** | Smooth Zoom In / Zoom Out |
| **White Squares** | Drag handles to resize selected shape |
| **Delete Key** | Remove the selected mapped area |
| **Copy Button** | Copy all generated HTML tags to clipboard |

---

## 🛠️ Tech Stack

* **Language:** Java 17+
* **Framework:** Swing / AWT
* **Rendering Engine:** Advanced `Graphics2D` (AffineTransform & World-to-Screen mapping)
* **Architecture:** Event-driven logic with custom `ShapeData` structures.

---

## 🚀 Getting Started

### Prerequisites
* **JDK 17** or higher installed on your system.

### Installation & Run
1. Clone the repository or download `HtmlHelperGodMode.java`.
2. Compile the source code:
   ```bash
   javac HtmlHelperGodMode.java
