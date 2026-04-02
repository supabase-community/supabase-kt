Deno.serve((_req) => {
  const encoder = new TextEncoder()
  const stream = new ReadableStream({
    start(controller) {
      controller.enqueue(encoder.encode("data: hello\n\n"))
      controller.enqueue(encoder.encode("data: world\n\n"))
      controller.enqueue(encoder.encode("data: done\n\n"))
      controller.close()
    },
  })
  return new Response(stream, {
    headers: { "Content-Type": "text/event-stream" },
  })
})
