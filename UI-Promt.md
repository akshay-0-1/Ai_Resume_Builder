Below is a ready-to-paste prompt you can feed to Gemini / GPT-4 / Claude (or any other code-capable model) to auto-redesign the entire React/Tailwind frontend.

---

SYSTEM  
You are an expert UI/UX designer and senior front-end engineer.  
Your task is to transform an existing React + Tailwind CSS web app called “AI-Powered Resume Builder & Job Matcher” into a modern, cohesive, delightfully animated experience while preserving all current functionality.

USER CONTEXT  
1. Tech stack  
   • React 18 with Vite  
   • Tailwind CSS + twin.macro utility classes  
   • Icons: Lucide-react  
   • State mgmt: React context + hooks (no Redux)  
   • Routing: React-router-dom 6  

2. High-level product flow  
   a. Auth → Dashboard  
   b. Dashboard split-screen: left (inputs) / right (outputs)  
   c. Secondary pages: Edit Resume, History, Pricing, About, Feedback, Landing  

3. Key components & their paths  
   • `pages/`  
     – `DashboardPage.jsx` (main analysis hub)  
     – `EditResumePage.jsx` (Formik multi-section resume editor)  
     – `History.jsx` (analysis history list)  
     – `PricingPage.jsx`, `LandingPage.jsx`, `AboutPage.jsx`, `FeedbackPage.jsx`, auth pages  
   • `components/`  
     – dashboard/: `ResumeUploader`, `JobDescriptionInput`, `AnalysisDisplay`, `ResumeDisplay`, `JobScoreCircle`, `TargetedChangeCard`  
     – common/: `Card`, `Button`, `Modal`, `Spinner`  
     – forms/: `ResumeSectionsFormV2` & sub-section components  
   • `context/AnalysisContext.jsx` (global state incl. `analysisResult`)  

4. Pain points you MUST fix  
   • Split-screen layout wastes space on ultrawide & mobile – add responsive breakpoints & smart stacking.  
   • Some cards leave huge blank areas; balance grid and flex usage.  
   • Typography hierarchy is flat; introduce clear H1-H5 scales and better line-length.  
   • No design tokens – define palette, spacing, radius, shadow, Z index.  
   • Few micro-interactions – add subtle motion (fade-in, slide-up, button hover-lift, score dial spin-in, etc.).  
   • Dark-mode missing – implement Tailwind dark variant with matching theme colors.  
   • Forms feel vanilla – add field focus glow, floating labels, progress stepper on Edit Resume.  
   • Empty states use plain text – replace with friendly illustration & call-to-action.  
   • No global Toast position/theme coherence – unify.  

DESIGN GOALS & AESTHETIC  
• Clean, professional (think Notion × Linear), but warm and approachable.  
• Primary color: #3B82F6 (blue-500) | Accent: #A855F7 (purple-500) | Success: #10B981 | Warning: #F59E0B | Danger: #EF4444.  
• Light & dark themes, auto-switch via OS preference, toggle in navbar.  
• Rounded-lg corners, medium elevation on hover.  
• Motion guidelines: duration 250-400 ms, cubic-bezier(0.4,0,0.2,1).  

REQUIRED OUTPUT FROM YOU (THE AI)  
Generate a single Markdown document containing:

1. “Global Changes” section  
   – Tailwind config additions (theme.extend), font families (e.g., Inter, JetBrains Mono for code), new CSS utilities if any.  
   – Added npm deps (e.g., framer-motion, react-hook-form).  

2. “Component Refactors” section  
   For EVERY file above that needs UI work, provide:  
   • Filename (code fence title)  
   • Full new component code (self-contained, ready to overwrite).  
   • Inline comments where logic stayed same but UI changed.  
   • Use framer-motion for animations where relevant.  
   • Keep props and external API identical unless change is absolutely required; then note migration instructions.  

3. “New Shared Components” section  
   – e.g., `components/common/SectionHeader.jsx`, `components/common/EmptyState.jsx`, `components/common/ThemeToggle.jsx`.  

4. “Routing/Layout Updates”  
   – Updated `App.jsx` / `Navbar` / responsive sidebar or drawer.  

5. “Design Tokens Reference” table.  

6. “Change-log Summary” bullet list (for PR description).  

CONSTRAINTS  
• Must compile with current Vite setup.  
• Do NOT introduce className/prop names longer than 50 chars.  
• Preserve existing accessibility (aria-labels, keyboard nav); improve where easy.  
• Keep all text static; do NOT invent new copy except for empty-state placeholders.  
• Your Markdown must not contain anything outside the specified structure.  

EVALUATION GUIDELINE  
Success = running `npm run dev` shows the same business logic but with the refreshed UI, no console errors, all tests pass.

## Incremental UI Modernization Plan (Phase-wise)

**Phase 1 – Design Tokens & Dependencies (Day 1)**
1. Extend `tailwind.config.js` with color palette, spacing, radius, z-index, shadows and Inter / JetBrains Mono fonts.
2. Add npm deps: `framer-motion`, `react-hook-form`, `react-toastify`, `clsx`.
3. Create `src/styles/tokens.js` exporting JS constants mirroring Tailwind theme.
4. Switch Tailwind to `class` strategy for dark mode; add `ThemeProvider` helper.

**Phase 2 – Layout Shell (Day 2)**
1. Introduce `Layout.jsx` (responsive navbar + optional sidebar / drawer).
2. Wrap all pages in the new layout via `App.jsx` routing changes.
3. Add `ThemeToggle` & global `ToastProvider` to the shell.

**Phase 3 – Critical Screens Refactor (Days 3-4)**
1. `DashboardPage`: convert split-screen to `grid md:grid-cols-2 xl:grid-cols-3`, add motion transitions to panels.
2. `ResumeUploader`, `JobDescriptionInput`, `AnalysisDisplay`, `ResumeDisplay`, `JobScoreCircle`, `TargetedChangeCard`: refactor visual styling, add hover & entrance animations.
3. Ensure responsive stacking on xs / sm breakpoints.

**Phase 4 – Edit Resume UX Upgrade (Day 5)**
1. Replace current long form with stepper wizard using `react-hook-form`.
2. Float labels & focus glow on fields.
3. Persist draft in localStorage.

**Phase 5 – Secondary Pages & Empty States (Day 6)**
1. Update `History`, `Pricing`, `About`, `Feedback`, `Landing`.
2. Add `EmptyState` illustration component used across pages.
3. Introduce `SectionHeader` for consistent typography hierarchy.

**Phase 6 – Polish & QA (Day 7)**
1. Lighthouse accessibility / performance audit; fix contrasts & aria attributes.
2. Visual regression screenshots with Storybook (stretch goal).
3. Final pass on animations duration / easing (250-400 ms, cubic-bezier(0.4,0,0.2,1)).

### Example Immediate Refactor Deliverable
Below is the redesigned `Button.jsx` component demonstrating tokens, dark-mode, and motion. Use this as the coding template for other shared components.

```jsx title="src/components/common/Button.jsx"
import { forwardRef } from 'react';
import { cva } from 'class-variance-authority';
import { motion } from 'framer-motion';
import clsx from 'clsx';

const buttonStyles = cva(
  'inline-flex items-center justify-center font-medium rounded-lg transition-colors focus:outline-none focus-visible:ring focus-visible:ring-offset-2',
  {
    variants: {
      variant: {
        primary:
          'bg-blue-500 text-white hover:bg-blue-600 active:bg-blue-700 focus-visible:ring-blue-400 dark:bg-blue-600 dark:hover:bg-blue-500',
        secondary:
          'bg-transparent border border-blue-500 text-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900 active:bg-blue-100',
      },
      size: {
        sm: 'px-3 py-1.5 text-sm',
        md: 'px-4 py-2',
        lg: 'px-6 py-3 text-lg',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md',
    },
  }
);

export const Button = forwardRef(({ variant, size, className, children, ...props }, ref) => (
  <motion.button
    whileHover={{ y: -2 }}
    whileTap={{ scale: 0.97 }}
    className={clsx(buttonStyles({ variant, size }), className)}
    ref={ref}
    {...props}
  >
    {children}
  </motion.button>
));

Button.displayName = 'Button';
```

_Phase 1 work can start immediately after dependency install; subsequent phases can proceed in parallel if multiple contributors are available._


---
