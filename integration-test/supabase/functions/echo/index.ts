Deno.serve(async (req) => {
  const body = await req.json()
  return new Response(JSON.stringify(body), {
    headers: { "Content-Type": "application/json" },
  })
})
