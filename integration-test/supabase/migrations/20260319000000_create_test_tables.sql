-- Test items table for PostgREST integration tests
CREATE TABLE IF NOT EXISTS public.test_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    user_id UUID REFERENCES auth.users(id)
);

ALTER TABLE public.test_items ENABLE ROW LEVEL SECURITY;

-- Authenticated users can CRUD their own rows
CREATE POLICY "Users can insert own items"
    ON public.test_items FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own items"
    ON public.test_items FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Users can update own items"
    ON public.test_items FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own items"
    ON public.test_items FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);

-- Anon users can read all rows
CREATE POLICY "Anon can read all items"
    ON public.test_items FOR SELECT
    TO anon
    USING (true);

-- Storage bucket for integration tests
INSERT INTO storage.buckets (id, name, public)
VALUES ('test-bucket', 'test-bucket', false)
ON CONFLICT (id) DO NOTHING;

-- Storage policies for authenticated users
CREATE POLICY "Authenticated users can upload to test-bucket"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'test-bucket');

CREATE POLICY "Authenticated users can read from test-bucket"
    ON storage.objects FOR SELECT
    TO authenticated
    USING (bucket_id = 'test-bucket');

CREATE POLICY "Authenticated users can delete from test-bucket"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (bucket_id = 'test-bucket');
