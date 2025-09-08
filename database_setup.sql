-- Supabase Database Setup for PES App
-- Run these SQL commands in your Supabase SQL Editor

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'GUEST',
    contact_number TEXT DEFAULT '',
    profile_image TEXT DEFAULT '',
    children TEXT[] DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create events table
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    event_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    location TEXT DEFAULT '',
    category TEXT NOT NULL DEFAULT 'GENERAL',
    image_url TEXT DEFAULT '',
    is_active BOOLEAN DEFAULT TRUE,
    author_id UUID REFERENCES users(id),
    author_name TEXT DEFAULT '',
    attendees TEXT[] DEFAULT '{}',
    max_attendees INTEGER,
    tags TEXT[] DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create announcements table
CREATE TABLE IF NOT EXISTS announcements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    author_id UUID REFERENCES users(id),
    author_name TEXT DEFAULT '',
    category TEXT DEFAULT 'GENERAL',
    priority TEXT DEFAULT 'MEDIUM',
    is_active BOOLEAN DEFAULT TRUE,
    target_roles TEXT[] DEFAULT '{"PARENT","TEACHER","ADMIN"}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create financial_reports table
CREATE TABLE IF NOT EXISTS financial_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    amount DECIMAL(10,2) NOT NULL,
    report_type TEXT NOT NULL DEFAULT 'INCOME',
    category TEXT DEFAULT 'OTHER',
    report_date TIMESTAMP WITH TIME ZONE NOT NULL,
    author_id UUID REFERENCES users(id),
    author_name TEXT DEFAULT '',
    attachments TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security (RLS)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE events ENABLE ROW LEVEL SECURITY;
ALTER TABLE announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE financial_reports ENABLE ROW LEVEL SECURITY;

-- RLS Policies for users table
CREATE POLICY "Users can view all profiles" ON users FOR SELECT USING (true);
CREATE POLICY "Users can update own profile" ON users FOR UPDATE USING (auth.uid()::text = id::text);
CREATE POLICY "Admins can insert users" ON users FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role = 'ADMIN')
);
CREATE POLICY "Admins can delete users" ON users FOR DELETE USING (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role = 'ADMIN')
);

-- RLS Policies for events table
CREATE POLICY "Anyone can view active events" ON events FOR SELECT USING (is_active = true);
CREATE POLICY "Admins and teachers can create events" ON events FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role IN ('ADMIN', 'TEACHER'))
);
CREATE POLICY "Admins and teachers can update events" ON events FOR UPDATE USING (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role IN ('ADMIN', 'TEACHER'))
);
CREATE POLICY "Admins can delete events" ON events FOR DELETE USING (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role = 'ADMIN')
);

-- RLS Policies for announcements table
CREATE POLICY "Anyone can view active announcements" ON announcements FOR SELECT USING (is_active = true);
CREATE POLICY "Admins and teachers can create announcements" ON announcements FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role IN ('ADMIN', 'TEACHER'))
);
CREATE POLICY "Admins and teachers can update announcements" ON announcements FOR UPDATE USING (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role IN ('ADMIN', 'TEACHER'))
);
CREATE POLICY "Admins can delete announcements" ON announcements FOR DELETE USING (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role = 'ADMIN')
);

-- RLS Policies for financial_reports table
CREATE POLICY "Anyone can view active financial reports" ON financial_reports FOR SELECT USING (is_active = true);
CREATE POLICY "Admins can create financial reports" ON financial_reports FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role = 'ADMIN')
);
CREATE POLICY "Admins can update financial reports" ON financial_reports FOR UPDATE USING (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role = 'ADMIN')
);
CREATE POLICY "Admins can delete financial reports" ON financial_reports FOR DELETE USING (
    EXISTS (SELECT 1 FROM users WHERE id::text = auth.uid()::text AND role = 'ADMIN')
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_events_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_active ON events(is_active);
CREATE INDEX IF NOT EXISTS idx_events_category ON events(category);
CREATE INDEX IF NOT EXISTS idx_announcements_active ON announcements(is_active);
CREATE INDEX IF NOT EXISTS idx_announcements_created ON announcements(created_at);
CREATE INDEX IF NOT EXISTS idx_financial_reports_date ON financial_reports(report_date);
CREATE INDEX IF NOT EXISTS idx_financial_reports_type ON financial_reports(report_type);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Add triggers for updated_at columns
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_events_updated_at BEFORE UPDATE ON events FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_announcements_updated_at BEFORE UPDATE ON announcements FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_financial_reports_updated_at BEFORE UPDATE ON financial_reports FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data (optional)
INSERT INTO users (email, display_name, role, contact_number) VALUES
    ('admin@pesapp.com', 'System Administrator', 'ADMIN', '+63 123 456 7890'),
    ('teacher@pesapp.com', 'Sample Teacher', 'TEACHER', '+63 123 456 7891'),
    ('parent@pesapp.com', 'Sample Parent', 'PARENT', '+63 123 456 7892')
ON CONFLICT (email) DO NOTHING;

-- Insert sample events
INSERT INTO events (title, description, event_date, location, category, author_name, tags) VALUES
    ('Coco Lilay Festival 2025', 'Annual school festival with cultural performances and food stalls. Students showcase traditional dances and local products.', NOW() + INTERVAL '30 days', 'School Grounds', 'CULTURAL', 'School Admin', ARRAY['festival', 'cultural', 'celebration']),
    ('Parent-Teacher Meeting', 'Quarterly meeting to discuss student progress and school activities.', NOW() + INTERVAL '7 days', 'Classrooms', 'MEETING', 'School Admin', ARRAY['meeting', 'parent', 'teacher']),
    ('Rape Prevention Lecture', 'PMSg Mary Ann A Limbo conducted a lecture about rape prevention tips and other gender-based cases among the teachers and students.', NOW() + INTERVAL '15 days', 'School Auditorium', 'WORKSHOP', 'School Admin', ARRAY['safety', 'education', 'awareness'])
ON CONFLICT DO NOTHING;
