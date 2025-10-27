# LitClub

> A self-hosted platform for collaborative reading and book club management

## Overview

LitClub is a digital platform designed to bridge the gap between personal library management and the communal spirit of traditional book clubs. Unlike centralized services like Goodreads, LitClub gives small communities—classrooms, families, reading societies—complete control over their data and reading experience.

## The Problem

Current reading platforms fall into two camps:
- **Cataloging platforms** (Goodreads, StoryGraph) excel at tracking books but lack meaningful community features
- **Communication tools** (Teams, WhatsApp) enable discussion but aren't built for literary engagement

Readers need a unified space that supports both personal library management and rich collaborative features—all while maintaining autonomy over their data.

## What LitClub Offers

**Personal Library Management**
- Track books you're reading, want to read, and have completed
- Write private notes and reviews
- Organize your reading history

**Collaborative Features**
- Create and join book clubs with role-based permissions
- Schedule virtual and in-person meetings
- Share notes and discussion points within your group
- Participate in structured literary discussions

**Self-Hosted Architecture**
- Deploy on your own server or machine
- Full control over your data
- Customizable for different contexts (educational, personal, community)
- Export and import data for portability

## Project Structure

```
LitClub/
├── Backend/          # Spring Boot REST API
├── Desktop/          # JavaFX desktop client
```

The architecture separates the backend API from frontend clients, allowing for:
- A feature-rich desktop application (JavaFX)
- Future web client for basic access
- Potential for mobile clients

## Current Status

🚧 **In Active Development** 🚧

LitClub is being developed as part of SWE 3090 (Fall 2025) at [University]. Core features are being implemented following the Waterfall SDLC methodology.

**What's Working:**
- User authentication and authorization
- Personal library management
- Book club creation and membership
- Meeting scheduling
- Notes system (private and shared)
- Theme system (light/dark mode)

**What's Coming:**
- Discussion prompts and replies
- Recommendation system
- Member management tools
- Enhanced book metadata handling
- Web client interface

## Philosophy

LitClub is built on the belief that reading is both a personal journey and a shared experience. By putting control back in the hands of readers, we enable:
- **Privacy**: Your reading data belongs to you
- **Flexibility**: Customize the platform for your community's needs
- **Intimacy**: Small, purpose-driven reading communities without corporate oversight
- **Accessibility**: Self-hosting options for institutions and individuals

## Technology Stack

**Backend:** Spring Boot, PostgreSQL, JWT Authentication  
**Desktop Client:** JavaFX, Gson for caching  
**Planned Web Client:** Thymeleaf (server-rendered views)

## Target Users

- **Educators** managing classroom reading groups
- **Book clubs** wanting autonomy from corporate platforms
- **Families** tracking shared reading experiences
- **Reading communities** in Africa and beyond seeking locally-controlled solutions


*"Reading has always been a personal and communal activity."*
