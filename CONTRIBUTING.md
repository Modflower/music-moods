# Contributing

Hi, you actually want to contribute to the mess of C preprocessor macros and INI files? Well, alright.

You should ideally make your new contribution work on all the supported versions,
and target your PRs to the `cherry` branch. If you can't make it work for every version,
just do your best effort to make it work for one version.

If you need help with something, reach feel free to out.

## Required tooling

You will most likely need specific versions of tooling to be able to hack on Music Moods effectively,
as we use Manifold and a bespoke Gradle script setup, including using INI files and Stonecutter.

We use the following for our IDE, as it has been the only setup that doesn't immediately implode in our experience:

- IntelliJ IDEA 2023.3.8
	- Manifold 2023.3.37
	- Minecraft Development 2023.3-1.8.1
	- Ini 233.15619.11

You shall avoid using any generative AI based tooling, including plugins, while contributing to Music Moods.

## Generative AI Policy

### The definition

Generative AI shall include but is not limited to: Artificial Intelligence ("AI"), Large Language Model ("LLM"),
and related technologies. Other generative forms of Machine Learning ("ML") algorithms are included,
and may include any ML tooling that in any way synthesizes or constructs work that did not exist before by using models.

Destructive and same-source ML algorithms, such as selectors, croppers, denoisers, and same-source clone tools,
are permitted provided it is not synthesizing nor constructing work from an external model.

Classical machine learning algorithms, such as those found within older IDEs and editors, are permitted provided that
it solely build a local model based off your own input and the immediate work for the purposes of providing minor code
suggestions.

Template-based work generators, provided the work and the generator is not otherwise generative AI or produced by it,
do not count towards this policy.

Given the above definitions, Generative AI includes, but is not limited to:

- OpenAI ChatGPT
- Microsoft Copilot, including Bing's Copilot Search
- GitHub Copilot
- Google Gemini, including Google Search's AI Overview
- DuckDuckGo Duck.AI, including DuckDuckGo's Search Assist
- Anthropic Claude
- DeepSeek
- Any AI overviews and summaries, such as those provided by your search provider and browser.

### The rules

Any work, including but not limited to code, documentation, artworks/assets, commits and issues,
that was generated, assisted or otherwise produced by Generative AI is strictly prohibited.

This shall extend to any non-required dependencies.
Exceptions may be granted on a case by case basis to dependencies with an enforced strict No-AI policy,
security updates that affect the work, and required dependencies, such as the work being modded, the modding platform,
and build tooling required to work with the modding platform and the work to be modded. This currently includes:

- Gradle
	- By extension: This includes Kotlin for the Gradle buildscripts.
- Minecraft
	- By nature of being a mod for Minecraft, this is strictly unavoidable.
- The modding platforms, Fabric, Forge and NeoForge, including their dependencies.
	- Alternative modding platforms typically don't last long or quickly diverge into their own APIs.
	  We cannot rely on no-AI forks for compatibility unless they manage to surpass the original.
- Any competing modifications for adding compatibility as necessary.
	- Unavoidably, if competing modifications are popular and too extensive, we may need to depend on the modifications
	  involved to suitably modify their flow to fit ours. Avoid this if at all possible by using indirect mixining
	  strategies.

Any dependencies that are actively using generative AI as part of their work shall have a No-AI replacement sought out,
and fitted into place wherever reasonable and timely possible.

Any contributions by submitters with a known history of using generative AI,
or with no prior contribution history on the code hosting platform, may be held to stricter scrutiny.

Avoid feeding this work to any Generative AI and instead ask the developers, your peers and read relevant documentation.
Not only does this reduce wasting resources
needlessly[^ai-datacenters][^ram-crisis-wikipedia][^power-eesi][^power-ncsu][^power-osti],
you get to learn the code more intimately than you would've otherwise had you just fed it to a machine.

### The reasoning

We believe this to be the best position to take given all the ethical and quality concerns AI have.
We have witnessed great software projects that once were very stable and bug free succumb to some of the strangest bugs,
and don't wish to perpetuate technology that is not only unethically trained,
but often used for copyright laundering due to the uncertain status of AI output.

Supporting or merely condoning AI means supporting eroding resources we're already burning through,
eroding trust that was previously earned, and eroding the quality of everything we allow it to touch.

How LLMs are trained is often done in an unethical manner[^ethics-huggingface], resorting to
scraping[^scrape-huggingface]
and including works that never permitted to be included in a database or model,
and requires immense resources that see very little practical
return.[^ai-datacenters][^ram-crisis-wikipedia][^power-eesi][^power-ncsu][^power-osti]

The 2025 RAM crisis is a direct result of generative AI.[^ram-crisis-wikipedia]

Your power bill increasing is a direct result of generative AI.[^power-eesi][^power-ncsu][^power-osti]

Software that used to work just a few years ago as of 2026 and now are dysfunctional are a direct result
of generative AI.[^code-gitclear-maintenance][^code-ethical-foss]

Websites having more aggressive anti-botting measures is a direct result of generative
AI.[^crawl-devault][^crawl-unc][^crawl-codeberg][^crawl-cloudflare]
You've likely seen this in the forms of Anubis, go-away and Cloudflare Turnstile intruding into your daily web browsing,
or various news agencies now pay-walling far more content than they used to.

The disbelief and distrust in media in the modern day is a direct result of generative AI[^gen-nbc],
resulting in multiple no-AI alternatives[^noai-ddg][^noai-ethical-foss] and blocklists[^noai-stevos][^noai-laylavish].

[^ai-datacenters]: Datacenters dedicated to generative AI are infamously very power
demanding[^power-eesi][^power-ncsu][^power-osti]
and often disrupts local communities by making it more expensive to live there by increasing power bills and local
taxes.
It also suppresses job opportunity in the long term as the only enrichment any datacenter provides for jobs once
built[^jobs-forbes],
can often be managed by a small IT department of the company that built the datacenter and a security firm,
which would only need to hire only one to two jobs per entryway.

[^ethics-huggingface]: https://github.com/bigcode-project/opt-out-v2/pull/3724

[^scrape-huggingface]: https://huggingface.co/datasets/HuggingFaceCode/stack-v3-train#data-collection

[^ram-crisis-wikipedia]: https://en.wikipedia.org/wiki/2025-present_global_memory_supply_shortage

[^power-eesi]: https://www.eesi.org/articles/view/data-center-power-demands-are-contributing-to-higher-energy-bills

[^power-ncsu]: https://news.ncsu.edu/2026/05/data-centers-power-bills/

[^power-osti]: https://www.osti.gov/biblio/3374245

[^code-gitclear-maintenance]: https://www.gitclear.com/the_ai_code_quality_maintainability_gap

[^code-ethical-foss]: A source of
sources: https://codeberg.org/ethical-foss/open-slopware/src/branch/main/why_not_llms.md#poor-code-quality

[^gen-nbc]: https://www.nbcnews.com/tech/tech-news/experts-warn-collapse-trust-online-ai-deepfakes-venezuela-rcna252472

[^crawl-devault]: https://drewdevault.com/blog/Stop-externalizing-your-costs-on-me/

[^crawl-unc]: https://library.unc.edu/news/library-it-vs-the-ai-bots/

[^crawl-codeberg]: https://social.anoxinon.de/@Codeberg/115033790447125787

[^crawl-cloudflare]: https://blog.cloudflare.com/firewall-for-ai/

[^noai-ddg]: https://noai.duckduckgo.com

[^noai-ethical-foss]: A repository of all FOSS that condones or uses generative AI, and any of its
alternatives: https://codeberg.org/ethical-foss/open-slopware

[^noai-stevos]: An AdBlock Plus filter list dedicated to filtering AI
features: https://github.com/Stevoisiak/Stevos-AI-Blocklist

[^noai-laylavish]: A uBlacklist filter dedicated to filtering AI
websites: https://github.com/laylavish/uBlockOrigin-HUGE-AI-Blocklist

[^jobs-forbes]: It is notable that most jobs mentioned within this article by Forbes:
https://www.forbes.com/sites/lucianapaulise/2025/12/11/the-data-center-jobs-boom-a-high-paying-career-pivot-opportunity/
are all construction-time jobs, with the only long term jobs being software engineering, an already saturated field,
and operators & maintenance staff, which only requires on-demand IT departments and very few on-site personnel
once the datacenter is set up and running.

## Agreement

By contributing to this project, you agree that:

- You are the copyright holder of the work you are contributing to the project.
- Your work is not a product of Generative AI. See [Generative AI Policy](#generative-ai-policy) for more details.
- If your work is a derivative work: You have the permission from the original work's authors and copyright holders to
  include their work in this project.
	- This may include the original work being licensed under a suitable license for the repository.
	- You shall be responsible for updating and including all copyright notices as mandated by the original work.
	- If the original work has no copyright notices, you should include notices as appropriate for the license of the
	  original work.
- You are contributing the work under the same license as the repository, OR a compatible license.
	- Music Moods is an [MPL-2.0](LICENSE) licensed project linking to [proprietary code](https://minecraft.net).
	  You shall ensure that the license permits linking to proprietary code AND can be licensed as effectively MPL-2.0.
